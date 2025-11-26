package com.itau.transferapi.domain.repository;

import com.itau.transferapi.domain.entity.DailyTransferLimit;
import com.itau.transferapi.domain.valueobject.AccountId;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Interface de repositório para operações com Limite Diário de Transferência.
 */
public interface DailyTransferLimitRepository {
    
    /**
     * Busca o limite diário de uma conta para uma data específica.
     * 
     * @param accountId ID da conta
     * @param date data
     * @return Optional contendo o limite ou vazio
     */
    Optional<DailyTransferLimit> findByAccountIdAndDate(AccountId accountId, LocalDate date);
    
    /**
     * Salva ou atualiza o limite diário.
     * 
     * @param dailyLimit limite a ser salvo
     * @return limite salvo
     */
    DailyTransferLimit save(DailyTransferLimit dailyLimit);
    
    /**
     * Busca o limite diário com lock para atualização.
     * 
     * @param accountId ID da conta
     * @param date data
     * @return Optional contendo o limite ou vazio
     */
    Optional<DailyTransferLimit> findByAccountIdAndDateForUpdate(AccountId accountId, LocalDate date);
}


