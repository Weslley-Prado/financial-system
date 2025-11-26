package com.itau.transferapi.infrastructure.adapter.output.persistence.repository;

import com.itau.transferapi.infrastructure.entity.DailyTransferLimitJpaEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositório JPA para operações com DailyTransferLimit.
 */
@Repository
public interface DailyTransferLimitJpaRepository extends JpaRepository<DailyTransferLimitJpaEntity, UUID> {
    
    Optional<DailyTransferLimitJpaEntity> findByAccountIdAndDate(UUID accountId, LocalDate date);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM DailyTransferLimitJpaEntity d WHERE d.accountId = :accountId AND d.date = :date")
    Optional<DailyTransferLimitJpaEntity> findByAccountIdAndDateForUpdate(
        @Param("accountId") UUID accountId,
        @Param("date") LocalDate date
    );
}


