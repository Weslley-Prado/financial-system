package com.itau.transferapi.domain.entity;

import com.itau.transferapi.domain.exception.BusinessException;
import com.itau.transferapi.domain.exception.ErrorCode;
import com.itau.transferapi.domain.valueobject.AccountId;
import com.itau.transferapi.domain.valueobject.Money;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Entidade de domínio que representa o controle de limite diário de transferências.
 * 
 * Regras de negócio:
 * - Limite diário de R$ 1.000,00 por conta
 * - Limite é resetado diariamente
 * - Acumula todas as transferências do dia
 */
@Getter
@Builder
public class DailyTransferLimit {
    
    private static final Money DEFAULT_DAILY_LIMIT = Money.of("1000.00");
    
    private final AccountId accountId;
    private final LocalDate date;
    private Money usedAmount;
    private Money dailyLimit;
    
    /**
     * Cria um novo controle de limite diário com valores padrão.
     * 
     * @param accountId ID da conta
     * @return novo controle de limite diário
     */
    public static DailyTransferLimit createDefault(AccountId accountId) {
        return DailyTransferLimit.builder()
            .accountId(accountId)
            .date(LocalDate.now())
            .usedAmount(Money.zero())
            .dailyLimit(DEFAULT_DAILY_LIMIT)
            .build();
    }
    
    /**
     * Calcula o limite disponível para novas transferências.
     * 
     * @return limite disponível
     */
    public Money getAvailableLimit() {
        return dailyLimit.subtract(usedAmount);
    }
    
    /**
     * Valida se o valor está dentro do limite diário disponível.
     * 
     * @param amount valor a ser transferido
     * @throws BusinessException se o limite diário for excedido
     */
    public void validateLimit(Money amount) {
        Objects.requireNonNull(amount, "Valor da transferência não pode ser nulo");
        
        Money newUsedAmount = usedAmount.add(amount);
        
        if (newUsedAmount.isGreaterThan(dailyLimit)) {
            throw new BusinessException(
                ErrorCode.DAILY_LIMIT_EXCEEDED,
                String.format("Limite diário excedido. Limite: %s, Utilizado: %s, Solicitado: %s, Disponível: %s",
                    dailyLimit.getFormattedValue(),
                    usedAmount.getFormattedValue(),
                    amount.getFormattedValue(),
                    getAvailableLimit().getFormattedValue())
            );
        }
    }
    
    /**
     * Registra o uso do limite diário.
     * 
     * @param amount valor utilizado
     */
    public void useLimit(Money amount) {
        validateLimit(amount);
        this.usedAmount = this.usedAmount.add(amount);
    }
    
    /**
     * Verifica se o limite ainda é válido para a data atual.
     * 
     * @return true se o limite é da data atual
     */
    public boolean isCurrentDate() {
        return LocalDate.now().equals(this.date);
    }
    
    /**
     * Obtém a porcentagem do limite utilizado.
     * 
     * @return porcentagem utilizada (0-100)
     */
    public double getUsagePercentage() {
        return usedAmount.getValue().doubleValue() / 
               dailyLimit.getValue().doubleValue() * 100;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DailyTransferLimit that = (DailyTransferLimit) o;
        return Objects.equals(accountId, that.accountId) && 
               Objects.equals(date, that.date);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(accountId, date);
    }
}


