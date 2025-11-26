package com.itau.transferapi.domain.entity;

import com.itau.transferapi.domain.exception.BusinessException;
import com.itau.transferapi.domain.exception.ErrorCode;
import com.itau.transferapi.domain.valueobject.AccountId;
import com.itau.transferapi.domain.valueobject.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

@DisplayName("DailyTransferLimit Entity Tests")
class DailyTransferLimitTest {
    
    private AccountId accountId;
    private DailyTransferLimit dailyLimit;
    
    @BeforeEach
    void setUp() {
        accountId = AccountId.generate();
        dailyLimit = DailyTransferLimit.builder()
            .accountId(accountId)
            .date(LocalDate.now())
            .usedAmount(Money.of("200.00"))
            .dailyLimit(Money.of("1000.00"))
            .build();
    }
    
    @Nested
    @DisplayName("Criação de Limite Diário")
    class CreationTests {
        
        @Test
        @DisplayName("Deve criar limite padrão corretamente")
        void shouldCreateDefaultLimit() {
            DailyTransferLimit limit = DailyTransferLimit.createDefault(accountId);
            
            assertThat(limit.getAccountId()).isEqualTo(accountId);
            assertThat(limit.getDate()).isEqualTo(LocalDate.now());
            assertThat(limit.getUsedAmount()).isEqualTo(Money.zero());
            assertThat(limit.getDailyLimit().getValue())
                .isEqualByComparingTo("1000.00");
        }
    }
    
    @Nested
    @DisplayName("Cálculo de Limite Disponível")
    class AvailableLimitTests {
        
        @Test
        @DisplayName("Deve calcular limite disponível corretamente")
        void shouldCalculateAvailableLimit() {
            Money available = dailyLimit.getAvailableLimit();
            
            assertThat(available.getValue())
                .isEqualByComparingTo("800.00");
        }
    }
    
    @Nested
    @DisplayName("Validação de Limite")
    class ValidateLimitTests {
        
        @Test
        @DisplayName("Deve passar quando dentro do limite")
        void shouldPassWhenWithinLimit() {
            assertThatNoException()
                .isThrownBy(() -> dailyLimit.validateLimit(Money.of("500.00")));
        }
        
        @Test
        @DisplayName("Deve lançar exceção quando excede limite")
        void shouldThrowWhenExceedsLimit() {
            assertThatThrownBy(() -> dailyLimit.validateLimit(Money.of("900.00")))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.DAILY_LIMIT_EXCEEDED);
        }
        
        @Test
        @DisplayName("Deve passar quando usa exatamente o limite disponível")
        void shouldPassWhenExactlyAtLimit() {
            assertThatNoException()
                .isThrownBy(() -> dailyLimit.validateLimit(Money.of("800.00")));
        }
    }
    
    @Nested
    @DisplayName("Uso do Limite")
    class UseLimitTests {
        
        @Test
        @DisplayName("Deve atualizar valor usado corretamente")
        void shouldUpdateUsedAmountCorrectly() {
            dailyLimit.useLimit(Money.of("300.00"));
            
            assertThat(dailyLimit.getUsedAmount().getValue())
                .isEqualByComparingTo("500.00");
        }
    }
    
    @Nested
    @DisplayName("Porcentagem de Uso")
    class UsagePercentageTests {
        
        @Test
        @DisplayName("Deve calcular porcentagem de uso corretamente")
        void shouldCalculateUsagePercentage() {
            double percentage = dailyLimit.getUsagePercentage();
            
            assertThat(percentage).isEqualTo(20.0);
        }
    }
}


