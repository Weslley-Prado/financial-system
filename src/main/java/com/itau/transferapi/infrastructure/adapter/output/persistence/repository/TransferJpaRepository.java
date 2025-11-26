package com.itau.transferapi.infrastructure.adapter.output.persistence.repository;

import com.itau.transferapi.infrastructure.entity.TransferJpaEntity;
import com.itau.transferapi.infrastructure.entity.TransferJpaEntity.TransferStatusJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repositório JPA para operações com Transfer.
 */
@Repository
public interface TransferJpaRepository extends JpaRepository<TransferJpaEntity, UUID> {
    
    List<TransferJpaEntity> findBySourceAccountIdAndCreatedAtBetween(
        UUID sourceAccountId,
        LocalDateTime startDate,
        LocalDateTime endDate
    );
    
    List<TransferJpaEntity> findByStatusAndBacenRetryCountLessThan(
        TransferStatusJpa status,
        int maxRetries
    );
    
    @Query("SELECT t FROM TransferJpaEntity t WHERE " +
           "(t.sourceAccountId = :accountId OR t.targetAccountId = :accountId) " +
           "AND t.createdAt BETWEEN :startDate AND :endDate")
    List<TransferJpaEntity> findByAccountIdAndDateRange(
        @Param("accountId") UUID accountId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
}


