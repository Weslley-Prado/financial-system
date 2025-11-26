package com.itau.transferapi.domain.repository;

import com.itau.transferapi.domain.entity.Transfer;
import com.itau.transferapi.domain.valueobject.AccountId;
import com.itau.transferapi.domain.valueobject.TransferId;
import com.itau.transferapi.domain.valueobject.TransferStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Interface de repositório para operações com Transferências.
 * 
 * Define o contrato para persistência de transferências,
 * seguindo o padrão Repository do DDD.
 */
public interface TransferRepository {
    
    /**
     * Busca uma transferência pelo seu ID.
     * 
     * @param transferId ID da transferência
     * @return Optional contendo a transferência ou vazio
     */
    Optional<Transfer> findById(TransferId transferId);
    
    /**
     * Salva ou atualiza uma transferência.
     * 
     * @param transfer transferência a ser salva
     * @return transferência salva
     */
    Transfer save(Transfer transfer);
    
    /**
     * Busca transferências por conta de origem.
     * 
     * @param sourceAccountId ID da conta de origem
     * @param startDate data inicial
     * @param endDate data final
     * @return lista de transferências
     */
    List<Transfer> findBySourceAccountIdAndDateRange(
        AccountId sourceAccountId, 
        LocalDateTime startDate, 
        LocalDateTime endDate
    );
    
    /**
     * Busca transferências pendentes de notificação ao BACEN.
     * 
     * @param status status a buscar
     * @param maxRetries máximo de tentativas
     * @return lista de transferências
     */
    List<Transfer> findByStatusAndRetryCountLessThan(TransferStatus status, int maxRetries);
    
    /**
     * Busca transferências por conta (origem ou destino).
     * 
     * @param accountId ID da conta
     * @param startDate data inicial
     * @param endDate data final
     * @return lista de transferências
     */
    List<Transfer> findByAccountIdAndDateRange(
        AccountId accountId, 
        LocalDateTime startDate, 
        LocalDateTime endDate
    );
}


