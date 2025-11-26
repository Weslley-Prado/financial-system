package com.itau.transferapi.domain.valueobject;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;
import java.util.Objects;

/**
 * Value Object imutável que representa um valor monetário.
 * 
 * Características:
 * - Imutável (thread-safe)
 * - Precisão de 2 casas decimais
 * - Operações aritméticas seguras
 * - Formatação em BRL
 */
@Getter
@EqualsAndHashCode
public final class Money {
    
    private static final Currency DEFAULT_CURRENCY = Currency.getInstance("BRL");
    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_EVEN;
    private static final Locale BRAZIL = new Locale("pt", "BR");
    private static final Money ZERO = new Money(BigDecimal.ZERO);
    
    private final BigDecimal value;
    private final Currency currency;
    
    private Money(BigDecimal value) {
        this(value, DEFAULT_CURRENCY);
    }
    
    private Money(BigDecimal value, Currency currency) {
        Objects.requireNonNull(value, "Valor não pode ser nulo");
        Objects.requireNonNull(currency, "Moeda não pode ser nula");
        
        this.value = value.setScale(SCALE, ROUNDING_MODE);
        this.currency = currency;
    }
    
    /**
     * Cria um Money a partir de uma string.
     * 
     * @param value valor como string
     * @return instância de Money
     */
    public static Money of(String value) {
        return new Money(new BigDecimal(value));
    }
    
    /**
     * Cria um Money a partir de um BigDecimal.
     * 
     * @param value valor
     * @return instância de Money
     */
    public static Money of(BigDecimal value) {
        return new Money(value);
    }
    
    /**
     * Cria um Money a partir de um double.
     * 
     * @param value valor
     * @return instância de Money
     */
    public static Money of(double value) {
        return new Money(BigDecimal.valueOf(value));
    }
    
    /**
     * Retorna uma instância de Money com valor zero.
     * 
     * @return Money com valor zero
     */
    public static Money zero() {
        return ZERO;
    }
    
    /**
     * Adiciona outro Money a este.
     * 
     * @param other outro Money
     * @return novo Money com a soma
     */
    public Money add(Money other) {
        validateSameCurrency(other);
        return new Money(this.value.add(other.value), this.currency);
    }
    
    /**
     * Subtrai outro Money deste.
     * 
     * @param other outro Money
     * @return novo Money com a diferença
     */
    public Money subtract(Money other) {
        validateSameCurrency(other);
        return new Money(this.value.subtract(other.value), this.currency);
    }
    
    /**
     * Multiplica por um fator.
     * 
     * @param factor fator de multiplicação
     * @return novo Money com o produto
     */
    public Money multiply(BigDecimal factor) {
        return new Money(this.value.multiply(factor), this.currency);
    }
    
    /**
     * Verifica se este valor é menor que outro.
     * 
     * @param other outro Money
     * @return true se este é menor
     */
    public boolean isLessThan(Money other) {
        validateSameCurrency(other);
        return this.value.compareTo(other.value) < 0;
    }
    
    /**
     * Verifica se este valor é maior que outro.
     * 
     * @param other outro Money
     * @return true se este é maior
     */
    public boolean isGreaterThan(Money other) {
        validateSameCurrency(other);
        return this.value.compareTo(other.value) > 0;
    }
    
    /**
     * Verifica se este valor é maior ou igual a outro.
     * 
     * @param other outro Money
     * @return true se este é maior ou igual
     */
    public boolean isGreaterThanOrEqual(Money other) {
        validateSameCurrency(other);
        return this.value.compareTo(other.value) >= 0;
    }
    
    /**
     * Verifica se o valor é positivo.
     * 
     * @return true se positivo
     */
    public boolean isPositive() {
        return this.value.compareTo(BigDecimal.ZERO) > 0;
    }
    
    /**
     * Verifica se o valor é zero.
     * 
     * @return true se zero
     */
    public boolean isZero() {
        return this.value.compareTo(BigDecimal.ZERO) == 0;
    }
    
    /**
     * Verifica se o valor é negativo.
     * 
     * @return true se negativo
     */
    public boolean isNegative() {
        return this.value.compareTo(BigDecimal.ZERO) < 0;
    }
    
    /**
     * Retorna o valor formatado em BRL.
     * 
     * @return valor formatado (ex: R$ 1.000,00)
     */
    public String getFormattedValue() {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(BRAZIL);
        formatter.setCurrency(currency);
        return formatter.format(value);
    }
    
    private void validateSameCurrency(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                String.format("Moedas diferentes: %s e %s", 
                    this.currency, other.currency)
            );
        }
    }
    
    @Override
    public String toString() {
        return getFormattedValue();
    }
}


