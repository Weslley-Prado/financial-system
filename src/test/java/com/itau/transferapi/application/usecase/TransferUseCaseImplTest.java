package com.itau.transferapi.application.usecase;

import com.itau.transferapi.application.dto.request.TransferRequest;
import com.itau.transferapi.application.dto.response.TransferResponse;
import com.itau.transferapi.application.port.output.BacenNotificationPort;
import com.itau.transferapi.application.port.output.ClientDataPort;
import com.itau.transferapi.domain.entity.Account;
import com.itau.transferapi.domain.entity.Client;
import com.itau.transferapi.domain.entity.DailyTransferLimit;
import com.itau.transferapi.domain.exception.BusinessException;
import com.itau.transferapi.domain.exception.ErrorCode;
import com.itau.transferapi.domain.repository.AccountRepository;
import com.itau.transferapi.domain.repository.DailyTransferLimitRepository;
import com.itau.transferapi.domain.repository.TransferRepository;
import com.itau.transferapi.domain.valueobject.AccountId;
import com.itau.transferapi.domain.valueobject.AccountStatus;
import com.itau.transferapi.domain.valueobject.Money;
import com.itau.transferapi.domain.valueobject.TransferStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransferUseCase Tests")
class TransferUseCaseImplTest {
    
    @Mock
    private AccountRepository accountRepository;
    
    @Mock
    private TransferRepository transferRepository;
    
    @Mock
    private DailyTransferLimitRepository dailyTransferLimitRepository;
    
    @Mock
    private ClientDataPort clientDataPort;
    
    @Mock
    private BacenNotificationPort bacenNotificationPort;
    
    @InjectMocks
    private TransferUseCaseImpl transferUseCase;
    
    private Account sourceAccount;
    private Account targetAccount;
    private Client client;
    private TransferRequest validRequest;
    
    @BeforeEach
    void setUp() {
        sourceAccount = createAccount("12345-6", "0001", 
            Money.of("5000.00"), Money.of("10000.00"), AccountStatus.ACTIVE);
        targetAccount = createAccount("98765-4", "0002", 
            Money.of("1000.00"), Money.of("5000.00"), AccountStatus.ACTIVE);
        
        client = Client.builder()
            .id(sourceAccount.getClientId())
            .name("João Silva")
            .documentNumber("12345678900")
            .active(true)
            .build();
        
        validRequest = TransferRequest.builder()
            .sourceAccountNumber("12345-6")
            .sourceAgencyNumber("0001")
            .targetAccountNumber("98765-4")
            .targetAgencyNumber("0002")
            .amount(new BigDecimal("150.00"))
            .build();
    }
    
    private Account createAccount(String number, String agency, 
            Money balance, Money limit, AccountStatus status) {
        return Account.builder()
            .id(AccountId.generate())
            .accountNumber(number)
            .agencyNumber(agency)
            .clientId(UUID.randomUUID())
            .balance(balance)
            .availableLimit(limit)
            .status(status)
            .createdAt(LocalDateTime.now())
            .build();
    }
    
    @Nested
    @DisplayName("Cenários de Sucesso")
    class SuccessScenarios {
        
        @Test
        @DisplayName("Deve realizar transferência com sucesso")
        void shouldExecuteTransferSuccessfully() {
            // Arrange
            when(accountRepository.findByAccountAndAgency("12345-6", "0001"))
                .thenReturn(Optional.of(sourceAccount));
            when(accountRepository.findByAccountAndAgency("98765-4", "0002"))
                .thenReturn(Optional.of(targetAccount));
            when(clientDataPort.findClientById(any()))
                .thenReturn(Optional.of(client));
            when(dailyTransferLimitRepository.findByAccountIdAndDateForUpdate(any(), any()))
                .thenReturn(Optional.empty());
            when(accountRepository.save(any())).thenAnswer(i -> i.getArgument(0));
            when(transferRepository.save(any())).thenAnswer(i -> i.getArgument(0));
            when(dailyTransferLimitRepository.save(any())).thenAnswer(i -> i.getArgument(0));
            when(bacenNotificationPort.notifyTransfer(any()))
                .thenReturn("BCN-12345678");
            
            // Act
            TransferResponse response = transferUseCase.execute(validRequest);
            
            // Assert
            assertThat(response).isNotNull();
            assertThat(response.status()).isEqualTo(TransferStatus.BACEN_NOTIFIED);
            assertThat(response.bacenNotificationId()).isEqualTo("BCN-12345678");
            assertThat(response.amount()).isEqualByComparingTo("150.00");
            
            verify(accountRepository, times(2)).save(any());
            verify(transferRepository, times(2)).save(any());
            verify(bacenNotificationPort).notifyTransfer(any());
        }
    }
    
    @Nested
    @DisplayName("Validações de Negócio")
    class BusinessValidations {
        
        @Test
        @DisplayName("Deve rejeitar transferência para mesma conta")
        void shouldRejectSameAccountTransfer() {
            TransferRequest sameAccountRequest = TransferRequest.builder()
                .sourceAccountNumber("12345-6")
                .sourceAgencyNumber("0001")
                .targetAccountNumber("12345-6")
                .targetAgencyNumber("0001")
                .amount(new BigDecimal("100.00"))
                .build();
            
            assertThatThrownBy(() -> transferUseCase.execute(sameAccountRequest))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.SAME_ACCOUNT_TRANSFER);
        }
        
        @Test
        @DisplayName("Deve rejeitar quando conta origem não existe")
        void shouldRejectWhenSourceAccountNotFound() {
            when(accountRepository.findByAccountAndAgency("12345-6", "0001"))
                .thenReturn(Optional.empty());
            
            assertThatThrownBy(() -> transferUseCase.execute(validRequest))
                .isInstanceOf(RuntimeException.class);
        }
        
        @Test
        @DisplayName("Deve rejeitar quando conta origem está inativa")
        void shouldRejectWhenSourceAccountIsInactive() {
            Account inactiveAccount = createAccount("12345-6", "0001", 
                Money.of("5000.00"), Money.of("10000.00"), AccountStatus.INACTIVE);
            
            when(accountRepository.findByAccountAndAgency("12345-6", "0001"))
                .thenReturn(Optional.of(inactiveAccount));
            when(accountRepository.findByAccountAndAgency("98765-4", "0002"))
                .thenReturn(Optional.of(targetAccount));
            when(clientDataPort.findClientById(any()))
                .thenReturn(Optional.of(client));
            
            assertThatThrownBy(() -> transferUseCase.execute(validRequest))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.ACCOUNT_NOT_ACTIVE);
        }
        
        @Test
        @DisplayName("Deve rejeitar quando saldo insuficiente")
        void shouldRejectWhenInsufficientBalance() {
            Account lowBalanceAccount = createAccount("12345-6", "0001", 
                Money.of("50.00"), Money.of("10000.00"), AccountStatus.ACTIVE);
            
            TransferRequest highAmountRequest = TransferRequest.builder()
                .sourceAccountNumber("12345-6")
                .sourceAgencyNumber("0001")
                .targetAccountNumber("98765-4")
                .targetAgencyNumber("0002")
                .amount(new BigDecimal("100.00"))
                .build();
            
            when(accountRepository.findByAccountAndAgency("12345-6", "0001"))
                .thenReturn(Optional.of(lowBalanceAccount));
            when(accountRepository.findByAccountAndAgency("98765-4", "0002"))
                .thenReturn(Optional.of(targetAccount));
            when(clientDataPort.findClientById(any()))
                .thenReturn(Optional.of(client));
            when(dailyTransferLimitRepository.findByAccountIdAndDateForUpdate(any(), any()))
                .thenReturn(Optional.empty());
            
            assertThatThrownBy(() -> transferUseCase.execute(highAmountRequest))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INSUFFICIENT_BALANCE);
        }
        
        @Test
        @DisplayName("Deve rejeitar quando limite diário excedido")
        void shouldRejectWhenDailyLimitExceeded() {
            DailyTransferLimit usedLimit = DailyTransferLimit.builder()
                .accountId(sourceAccount.getId())
                .date(LocalDate.now())
                .usedAmount(Money.of("950.00"))
                .dailyLimit(Money.of("1000.00"))
                .build();
            
            when(accountRepository.findByAccountAndAgency("12345-6", "0001"))
                .thenReturn(Optional.of(sourceAccount));
            when(accountRepository.findByAccountAndAgency("98765-4", "0002"))
                .thenReturn(Optional.of(targetAccount));
            when(clientDataPort.findClientById(any()))
                .thenReturn(Optional.of(client));
            when(dailyTransferLimitRepository.findByAccountIdAndDateForUpdate(any(), any()))
                .thenReturn(Optional.of(usedLimit));
            
            assertThatThrownBy(() -> transferUseCase.execute(validRequest))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.DAILY_LIMIT_EXCEEDED);
        }
    }
}


