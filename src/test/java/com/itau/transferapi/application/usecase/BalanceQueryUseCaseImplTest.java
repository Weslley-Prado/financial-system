package com.itau.transferapi.application.usecase;

import com.itau.transferapi.application.dto.response.BalanceResponse;
import com.itau.transferapi.application.port.output.ClientDataPort;
import com.itau.transferapi.domain.entity.Account;
import com.itau.transferapi.domain.entity.Client;
import com.itau.transferapi.domain.entity.DailyTransferLimit;
import com.itau.transferapi.domain.exception.BusinessException;
import com.itau.transferapi.domain.exception.ResourceNotFoundException;
import com.itau.transferapi.domain.repository.AccountRepository;
import com.itau.transferapi.domain.repository.DailyTransferLimitRepository;
import com.itau.transferapi.domain.valueobject.AccountId;
import com.itau.transferapi.domain.valueobject.AccountStatus;
import com.itau.transferapi.domain.valueobject.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("BalanceQueryUseCaseImpl Tests")
class BalanceQueryUseCaseImplTest {
    
    @Mock
    private AccountRepository accountRepository;
    
    @Mock
    private DailyTransferLimitRepository dailyTransferLimitRepository;
    
    @Mock
    private ClientDataPort clientDataPort;
    
    @InjectMocks
    private BalanceQueryUseCaseImpl balanceQueryUseCase;
    
    private Account activeAccount;
    private Account inactiveAccount;
    private Client activeClient;
    
    @BeforeEach
    void setUp() {
        UUID clientId = UUID.randomUUID();
        
        activeAccount = Account.builder()
            .id(AccountId.generate())
            .accountNumber("12345-6")
            .agencyNumber("0001")
            .clientId(clientId)
            .balance(Money.of("5000.00"))
            .availableLimit(Money.of("10000.00"))
            .status(AccountStatus.ACTIVE)
            .build();
        
        inactiveAccount = Account.builder()
            .id(AccountId.generate())
            .accountNumber("11111-1")
            .agencyNumber("0001")
            .clientId(UUID.randomUUID())
            .balance(Money.of("1000.00"))
            .availableLimit(Money.of("2000.00"))
            .status(AccountStatus.INACTIVE)
            .build();
        
        activeClient = Client.builder()
            .id(clientId)
            .name("João Silva")
            .documentNumber("12345678900")
            .active(true)
            .build();
    }
    
    @Nested
    @DisplayName("Consulta de Saldo")
    class GetBalanceTests {
        
        @Test
        @DisplayName("Deve retornar saldo com sucesso")
        void shouldReturnBalanceSuccessfully() {
            when(accountRepository.findByAccountAndAgency("12345-6", "0001"))
                .thenReturn(Optional.of(activeAccount));
            when(clientDataPort.findClientById(any()))
                .thenReturn(Optional.of(activeClient));
            when(dailyTransferLimitRepository.findByAccountIdAndDate(any(), any()))
                .thenReturn(Optional.empty());
            
            BalanceResponse response = balanceQueryUseCase.getBalance("12345-6", "0001");
            
            assertThat(response.accountNumber()).isEqualTo("12345-6");
            assertThat(response.holderName()).isEqualTo("João Silva");
            assertThat(response.balance()).isEqualByComparingTo(new BigDecimal("5000.00"));
            assertThat(response.availableLimit()).isEqualByComparingTo(new BigDecimal("10000.00"));
            assertThat(response.dailyTransferLimitAvailable()).isEqualByComparingTo(new BigDecimal("1000.00"));
        }
        
        @Test
        @DisplayName("Deve retornar limite restante correto")
        void shouldReturnCorrectRemainingLimit() {
            DailyTransferLimit limit = DailyTransferLimit.builder()
                .accountId(activeAccount.getId())
                .date(LocalDate.now())
                .usedAmount(Money.of("300.00"))
                .dailyLimit(Money.of("1000.00"))
                .build();
            
            when(accountRepository.findByAccountAndAgency("12345-6", "0001"))
                .thenReturn(Optional.of(activeAccount));
            when(clientDataPort.findClientById(any()))
                .thenReturn(Optional.of(activeClient));
            when(dailyTransferLimitRepository.findByAccountIdAndDate(any(), any()))
                .thenReturn(Optional.of(limit));
            
            BalanceResponse response = balanceQueryUseCase.getBalance("12345-6", "0001");
            
            assertThat(response.dailyTransferLimitAvailable())
                .isEqualByComparingTo(new BigDecimal("700.00"));
        }
        
        @Test
        @DisplayName("Deve lançar exceção para conta não encontrada")
        void shouldThrowExceptionForAccountNotFound() {
            when(accountRepository.findByAccountAndAgency(anyString(), anyString()))
                .thenReturn(Optional.empty());
            
            assertThatThrownBy(() -> balanceQueryUseCase.getBalance("99999-9", "9999"))
                .isInstanceOf(ResourceNotFoundException.class);
        }
        
        @Test
        @DisplayName("Deve lançar exceção para conta inativa")
        void shouldThrowExceptionForInactiveAccount() {
            when(accountRepository.findByAccountAndAgency("11111-1", "0001"))
                .thenReturn(Optional.of(inactiveAccount));
            
            assertThatThrownBy(() -> balanceQueryUseCase.getBalance("11111-1", "0001"))
                .isInstanceOf(BusinessException.class);
        }
        
        @Test
        @DisplayName("Deve usar fallback quando cliente não encontrado")
        void shouldUseFallbackWhenClientNotFound() {
            when(accountRepository.findByAccountAndAgency("12345-6", "0001"))
                .thenReturn(Optional.of(activeAccount));
            when(clientDataPort.findClientById(any()))
                .thenReturn(Optional.empty());
            when(dailyTransferLimitRepository.findByAccountIdAndDate(any(), any()))
                .thenReturn(Optional.empty());
            
            BalanceResponse response = balanceQueryUseCase.getBalance("12345-6", "0001");
            
            assertThat(response.holderName()).isEqualTo("Cliente");
        }
    }
}

