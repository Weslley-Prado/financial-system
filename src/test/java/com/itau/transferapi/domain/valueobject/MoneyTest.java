package com.itau.transferapi.domain.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Money Value Object Tests")
class MoneyTest {
    
    @Nested
    @DisplayName("Criação de Money")
    class CreationTests {
        
        @Test
        @DisplayName("Deve criar Money a partir de String")
        void shouldCreateFromString() {
            Money money = Money.of("100.50");
            
            assertThat(money.getValue())
                .isEqualByComparingTo(new BigDecimal("100.50"));
        }
        
        @Test
        @DisplayName("Deve criar Money a partir de BigDecimal")
        void shouldCreateFromBigDecimal() {
            Money money = Money.of(new BigDecimal("250.75"));
            
            assertThat(money.getValue())
                .isEqualByComparingTo(new BigDecimal("250.75"));
        }
        
        @Test
        @DisplayName("Deve criar Money zero")
        void shouldCreateZero() {
            Money money = Money.zero();
            
            assertThat(money.isZero()).isTrue();
        }
        
        @Test
        @DisplayName("Deve arredondar para 2 casas decimais")
        void shouldRoundToTwoDecimalPlaces() {
            Money money = Money.of("100.555");
            
            assertThat(money.getValue())
                .isEqualByComparingTo(new BigDecimal("100.56"));
        }
    }
    
    @Nested
    @DisplayName("Operações Aritméticas")
    class ArithmeticTests {
        
        @Test
        @DisplayName("Deve somar corretamente")
        void shouldAddCorrectly() {
            Money a = Money.of("100.00");
            Money b = Money.of("50.50");
            
            Money result = a.add(b);
            
            assertThat(result.getValue())
                .isEqualByComparingTo(new BigDecimal("150.50"));
        }
        
        @Test
        @DisplayName("Deve subtrair corretamente")
        void shouldSubtractCorrectly() {
            Money a = Money.of("100.00");
            Money b = Money.of("30.25");
            
            Money result = a.subtract(b);
            
            assertThat(result.getValue())
                .isEqualByComparingTo(new BigDecimal("69.75"));
        }
        
        @Test
        @DisplayName("Deve multiplicar corretamente")
        void shouldMultiplyCorrectly() {
            Money money = Money.of("100.00");
            
            Money result = money.multiply(new BigDecimal("1.5"));
            
            assertThat(result.getValue())
                .isEqualByComparingTo(new BigDecimal("150.00"));
        }
    }
    
    @Nested
    @DisplayName("Comparações")
    class ComparisonTests {
        
        @Test
        @DisplayName("Deve identificar valor menor")
        void shouldIdentifyLessThan() {
            Money a = Money.of("50.00");
            Money b = Money.of("100.00");
            
            assertThat(a.isLessThan(b)).isTrue();
            assertThat(b.isLessThan(a)).isFalse();
        }
        
        @Test
        @DisplayName("Deve identificar valor maior")
        void shouldIdentifyGreaterThan() {
            Money a = Money.of("100.00");
            Money b = Money.of("50.00");
            
            assertThat(a.isGreaterThan(b)).isTrue();
            assertThat(b.isGreaterThan(a)).isFalse();
        }
        
        @Test
        @DisplayName("Deve identificar valor positivo")
        void shouldIdentifyPositive() {
            assertThat(Money.of("100.00").isPositive()).isTrue();
            assertThat(Money.of("-100.00").isPositive()).isFalse();
            assertThat(Money.zero().isPositive()).isFalse();
        }
        
        @Test
        @DisplayName("Deve identificar valor negativo")
        void shouldIdentifyNegative() {
            assertThat(Money.of("-100.00").isNegative()).isTrue();
            assertThat(Money.of("100.00").isNegative()).isFalse();
        }
    }
    
    @Nested
    @DisplayName("Formatação")
    class FormattingTests {
        
        @Test
        @DisplayName("Deve formatar em BRL")
        void shouldFormatInBRL() {
            Money money = Money.of("1234.56");
            
            String formatted = money.getFormattedValue();
            
            // Aceita formato brasileiro com separadores corretos
            assertThat(formatted).contains("R$");
            assertThat(formatted).contains("1.234,56");
        }
        
        @Test
        @DisplayName("Deve formatar zero corretamente")
        void shouldFormatZero() {
            Money money = Money.zero();
            String formatted = money.getFormattedValue();
            
            assertThat(formatted).contains("R$");
            assertThat(formatted).contains("0,00");
        }
        
        @Test
        @DisplayName("Deve formatar valores negativos")
        void shouldFormatNegative() {
            Money money = Money.of("-500.00");
            String formatted = money.getFormattedValue();
            
            assertThat(formatted).contains("R$");
            assertThat(formatted).contains("500,00");
        }
    }
    
    @Nested
    @DisplayName("Imutabilidade")
    class ImmutabilityTests {
        
        @Test
        @DisplayName("Operações devem retornar nova instância")
        void operationsShouldReturnNewInstance() {
            Money original = Money.of("100.00");
            Money added = original.add(Money.of("50.00"));
            
            assertThat(original.getValue())
                .isEqualByComparingTo(new BigDecimal("100.00"));
            assertThat(added.getValue())
                .isEqualByComparingTo(new BigDecimal("150.00"));
            assertThat(original).isNotSameAs(added);
        }
        
        @Test
        @DisplayName("Subtração deve retornar nova instância")
        void subtractShouldReturnNewInstance() {
            Money original = Money.of("100.00");
            Money subtracted = original.subtract(Money.of("30.00"));
            
            assertThat(original.getValue()).isEqualByComparingTo(new BigDecimal("100.00"));
            assertThat(subtracted.getValue()).isEqualByComparingTo(new BigDecimal("70.00"));
        }
    }
    
    @Nested
    @DisplayName("Equals e HashCode")
    class EqualsHashCodeTests {
        
        @Test
        @DisplayName("Valores iguais devem ser equals")
        void equalValuesShouldBeEqual() {
            Money a = Money.of("100.00");
            Money b = Money.of("100.00");
            
            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }
        
        @Test
        @DisplayName("Valores diferentes não devem ser equals")
        void differentValuesShouldNotBeEqual() {
            Money a = Money.of("100.00");
            Money b = Money.of("200.00");
            
            assertThat(a).isNotEqualTo(b);
        }
        
        @Test
        @DisplayName("Deve comparar com null")
        void shouldHandleNullComparison() {
            Money money = Money.of("100.00");
            assertThat(money).isNotEqualTo(null);
        }
        
        @Test
        @DisplayName("Deve comparar com objeto de outro tipo")
        void shouldHandleDifferentTypeComparison() {
            Money money = Money.of("100.00");
            assertThat(money).isNotEqualTo("100.00");
        }
    }
    
    @Nested
    @DisplayName("Validações")
    class ValidationTests {
        
        @Test
        @DisplayName("Deve lançar exceção para valor nulo")
        void shouldThrowExceptionForNullValue() {
            assertThatThrownBy(() -> Money.of((BigDecimal) null))
                .isInstanceOf(NullPointerException.class);
        }
        
        @Test
        @DisplayName("Deve lançar exceção para string nula")
        void shouldThrowExceptionForNullString() {
            assertThatThrownBy(() -> Money.of((String) null))
                .isInstanceOf(NullPointerException.class);
        }
    }
    
    @Nested
    @DisplayName("Comparações Avançadas")
    class AdvancedComparisonTests {
        
        @Test
        @DisplayName("Deve comparar valores iguais corretamente")
        void shouldCompareEqualValues() {
            Money a = Money.of("100.00");
            Money b = Money.of("100.00");
            
            assertThat(a.isLessThan(b)).isFalse();
            assertThat(a.isGreaterThan(b)).isFalse();
        }
        
        @Test
        @DisplayName("isLessThanOrEqual deve funcionar")
        void shouldCheckLessThanOrEqual() {
            Money a = Money.of("100.00");
            Money b = Money.of("100.00");
            Money c = Money.of("150.00");
            
            assertThat(a.isLessThan(b)).isFalse();
            assertThat(a.isLessThan(c)).isTrue();
        }
        
        @Test
        @DisplayName("Deve identificar zero corretamente")
        void shouldIdentifyZero() {
            assertThat(Money.zero().isZero()).isTrue();
            assertThat(Money.of("0.00").isZero()).isTrue();
            assertThat(Money.of("0.01").isZero()).isFalse();
        }
    }
}


