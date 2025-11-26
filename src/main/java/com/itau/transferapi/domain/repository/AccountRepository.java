package com.itau.transferapi.domain.repository;

import com.itau.transferapi.domain.entity.Account;
import com.itau.transferapi.domain.valueobject.AccountId;

import java.util.Optional;
import java.util.UUID;

/**
 * Interface de repositório para operações com Contas.
 * 
 * Define o contrato para persistência de contas,
 * seguindo o padrão Repository do DDD.
 */
public interface AccountRepository {
    
    /**
     * Busca uma conta pelo seu ID.
     * 
     * @param accountId ID da conta
     * @return Optional contendo a conta ou vazio
     */
    Optional<Account> findById(AccountId accountId);
    
    /**
     * Busca uma conta pelo número da conta e agência.
     * 
     * @param accountNumber número da conta
     * @param agencyNumber número da agência
     * @return Optional contendo a conta ou vazio
     */
    Optional<Account> findByAccountAndAgency(String accountNumber, String agencyNumber);
    
    /**
     * Busca uma conta pelo ID do cliente.
     * 
     * @param clientId ID do cliente
     * @return Optional contendo a conta ou vazio
     */
    Optional<Account> findByClientId(UUID clientId);
    
    /**
     * Salva ou atualiza uma conta.
     * 
     * @param account conta a ser salva
     * @return conta salva
     */
    Account save(Account account);
    
    /**
     * Verifica se uma conta existe.
     * 
     * @param accountId ID da conta
     * @return true se existe
     */
    boolean existsById(AccountId accountId);
    
    /**
     * Busca uma conta com lock pessimista para atualização.
     * 
     * @param accountId ID da conta
     * @return Optional contendo a conta ou vazio
     */
    Optional<Account> findByIdForUpdate(AccountId accountId);
}


