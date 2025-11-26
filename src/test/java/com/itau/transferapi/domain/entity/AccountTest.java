package com.itau.transferapi.domain.entity;

import com.itau.transferapi.domain.exception.BusinessException;
import com.itau.transferapi.domain.exception.ErrorCode;
import com.itau.transferapi.domain.valueobject.AccountId;
import com.itau.transferapi.domain.valueobject.AccountStatus;
import com.itau.transferapi.domain.valueobject.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Account Entity Tests")
class AccountTest {
    
    private Account account;
    
    @BeforeEach
    void setUp() {
        account = createActiveAccount(
            Money.of("1000.00"),
            Money.of("5000.00")
        );
    }
    
    private Account createActiveAccount(Money balance, Money availableLimit) {
        return Account.builder()
            .id(AccountId.generate())
            .accountNumber("12345-6")
            .agencyNumber("0001")
            .clientId(UUID.randomUUID())
            .balance(balance)
            .availableLimit(availableLimit)
            .status(AccountStatus.ACTIVE)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }
    
    @Nested
    @DisplayName("Validação de Conta Ativa")
    class ValidateActiveTests {
        
        @Test
        @DisplayName("Deve passar quando conta está ativa")
        void shouldPassWhenAccountIsActive() {
            assertThatNoException().isThrownBy(() -> account.validateActive());
        }
        
        @Test
        @DisplayName("Deve lançar exceção quando conta está inativa")
        void shouldThrowWhenAccountIsInactive() {
            Account inactiveAccount = Account.builder()
                .id(AccountId.generate())
                .accountNumber("12345-6")
                .agencyNumber("0001")
                .clientId(UUID.randomUUID())
                .balance(Money.of("1000.00"))
                .availableLimit(Money.of("5000.00"))
                .status(AccountStatus.INACTIVE)
                .createdAt(LocalDateTime.now())
                .build();
            
            assertThatThrownBy(() -> inactiveAccount.validateActive())
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.ACCOUNT_NOT_ACTIVE);
        }
        
        @Test
        @DisplayName("Deve lançar exceção quando conta está bloqueada")
        void shouldThrowWhenAccountIsBlocked() {
            Account blockedAccount = Account.builder()
                .id(AccountId.generate())
                .accountNumber("12345-6")
                .agencyNumber("0001")
                .clientId(UUID.randomUUID())
                .balance(Money.of("1000.00"))
                .availableLimit(Money.of("5000.00"))
                .status(AccountStatus.BLOCKED)
                .createdAt(LocalDateTime.now())
                .build();
            
            assertThatThrownBy(() -> blockedAccount.validateActive())
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.ACCOUNT_NOT_ACTIVE);
        }
    }
    
    @Nested
    @DisplayName("Validação de Limite Disponível")
    class ValidateAvailableLimitTests {
        
        @Test
        @DisplayName("Deve passar quando limite é suficiente")
        void shouldPassWhenLimitIsSufficient() {
            assertThatNoException()
                .isThrownBy(() -> account.validateAvailableLimit(Money.of("1000.00")));
        }
        
        @Test
        @DisplayName("Deve lançar exceção quando limite é insuficiente")
        void shouldThrowWhenLimitIsInsufficient() {
            assertThatThrownBy(() -> account.validateAvailableLimit(Money.of("6000.00")))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INSUFFICIENT_LIMIT);
        }
    }
    
    @Nested
    @DisplayName("Validação de Saldo")
    class ValidateBalanceTests {
        
        @Test
        @DisplayName("Deve passar quando saldo é suficiente")
        void shouldPassWhenBalanceIsSufficient() {
            assertThatNoException()
                .isThrownBy(() -> account.validateBalance(Money.of("500.00")));
        }
        
        @Test
        @DisplayName("Deve lançar exceção quando saldo é insuficiente")
        void shouldThrowWhenBalanceIsInsufficient() {
            assertThatThrownBy(() -> account.validateBalance(Money.of("2000.00")))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INSUFFICIENT_BALANCE);
        }
    }
    
    @Nested
    @DisplayName("Operação de Débito")
    class DebitTests {
        
        @Test
        @DisplayName("Deve debitar corretamente")
        void shouldDebitCorrectly() {
            Money initialBalance = account.getBalance();
            Money debitAmount = Money.of("200.00");
            
            account.debit(debitAmount);
            
            assertThat(account.getBalance())
                .isEqualTo(initialBalance.subtract(debitAmount));
        }
        
        @Test
        @DisplayName("Deve falhar ao debitar conta inativa")
        void shouldFailToDebitInactiveAccount() {
            Account inactiveAccount = Account.builder()
                .id(AccountId.generate())
                .accountNumber("12345-6")
                .agencyNumber("0001")
                .clientId(UUID.randomUUID())
                .balance(Money.of("1000.00"))
                .availableLimit(Money.of("5000.00"))
                .status(AccountStatus.INACTIVE)
                .createdAt(LocalDateTime.now())
                .build();
            
            assertThatThrownBy(() -> inactiveAccount.debit(Money.of("100.00")))
                .isInstanceOf(BusinessException.class);
        }
    }
    
    @Nested
    @DisplayName("Operação de Crédito")
    class CreditTests {
        
        @Test
        @DisplayName("Deve creditar corretamente")
        void shouldCreditCorrectly() {
            Money initialBalance = account.getBalance();
            Money creditAmount = Money.of("500.00");
            
            account.credit(creditAmount);
            
            assertThat(account.getBalance())
                .isEqualTo(initialBalance.add(creditAmount));
        }
    }
}


