package com.itau.transferapi.domain.entity;

import com.itau.transferapi.domain.exception.BusinessException;
import com.itau.transferapi.domain.exception.ErrorCode;
import com.itau.transferapi.domain.valueobject.AccountId;
import com.itau.transferapi.domain.valueobject.AccountStatus;
import com.itau.transferapi.domain.valueobject.Money;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidade de domínio que representa uma Conta Corrente.
 * 
 * Esta classe segue os princípios do DDD (Domain-Driven Design):
 * - Entidade com identidade única (AccountId)
 * - Invariantes de negócio protegidas
 * - Comportamento rico encapsulado
 * 
 * Regras de negócio implementadas:
 * - Conta deve estar ativa para realizar operações
 * - Saldo disponível deve ser suficiente para transferência
 * - Validações de limite
 */
@Getter
@Builder
public class Account {
    
    private final AccountId id;
    private final String accountNumber;
    private final String agencyNumber;
    private final UUID clientId;
    private Money balance;
    private Money availableLimit;
    private AccountStatus status;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * Valida se a conta está ativa para realizar operações.
     * 
     * @throws BusinessException se a conta não estiver ativa
     */
    public void validateActive() {
        if (!AccountStatus.ACTIVE.equals(this.status)) {
            throw new BusinessException(
                ErrorCode.ACCOUNT_NOT_ACTIVE,
                String.format("Conta %s não está ativa. Status atual: %s", 
                    this.accountNumber, this.status)
            );
        }
    }
    
    /**
     * Valida se há limite disponível para realizar a transferência.
     * 
     * @param amount valor a ser transferido
     * @throws BusinessException se não houver limite disponível
     */
    public void validateAvailableLimit(Money amount) {
        Objects.requireNonNull(amount, "Valor da transferência não pode ser nulo");
        
        if (this.availableLimit.isLessThan(amount)) {
            throw new BusinessException(
                ErrorCode.INSUFFICIENT_LIMIT,
                String.format("Limite disponível insuficiente. Disponível: %s, Solicitado: %s",
                    this.availableLimit.getFormattedValue(), 
                    amount.getFormattedValue())
            );
        }
    }
    
    /**
     * Valida se há saldo suficiente para realizar a transferência.
     * 
     * @param amount valor a ser transferido
     * @throws BusinessException se não houver saldo suficiente
     */
    public void validateBalance(Money amount) {
        Objects.requireNonNull(amount, "Valor da transferência não pode ser nulo");
        
        if (this.balance.isLessThan(amount)) {
            throw new BusinessException(
                ErrorCode.INSUFFICIENT_BALANCE,
                String.format("Saldo insuficiente. Disponível: %s, Solicitado: %s",
                    this.balance.getFormattedValue(), 
                    amount.getFormattedValue())
            );
        }
    }
    
    /**
     * Debita um valor da conta.
     * 
     * @param amount valor a ser debitado
     * @throws BusinessException se a conta não estiver ativa ou sem saldo
     */
    public void debit(Money amount) {
        validateActive();
        validateBalance(amount);
        validateAvailableLimit(amount);
        
        this.balance = this.balance.subtract(amount);
        this.availableLimit = this.availableLimit.subtract(amount);
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Credita um valor na conta.
     * 
     * @param amount valor a ser creditado
     * @throws BusinessException se a conta não estiver ativa
     */
    public void credit(Money amount) {
        validateActive();
        Objects.requireNonNull(amount, "Valor do crédito não pode ser nulo");
        
        this.balance = this.balance.add(amount);
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Retorna o saldo formatado para exibição.
     * 
     * @return saldo formatado em BRL
     */
    public String getFormattedBalance() {
        return this.balance.getFormattedValue();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return Objects.equals(id, account.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}


