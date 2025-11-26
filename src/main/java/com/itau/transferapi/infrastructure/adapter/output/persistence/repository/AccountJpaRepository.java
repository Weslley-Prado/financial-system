package com.itau.transferapi.infrastructure.adapter.output.persistence.repository;

import com.itau.transferapi.infrastructure.entity.AccountJpaEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositório JPA para operações com Account.
 */
@Repository
public interface AccountJpaRepository extends JpaRepository<AccountJpaEntity, UUID> {
    
    Optional<AccountJpaEntity> findByAccountNumberAndAgencyNumber(
        String accountNumber, 
        String agencyNumber
    );
    
    Optional<AccountJpaEntity> findByClientId(UUID clientId);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM AccountJpaEntity a WHERE a.id = :id")
    Optional<AccountJpaEntity> findByIdForUpdate(@Param("id") UUID id);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM AccountJpaEntity a WHERE a.accountNumber = :accountNumber AND a.agencyNumber = :agencyNumber")
    Optional<AccountJpaEntity> findByAccountNumberAndAgencyNumberForUpdate(
        @Param("accountNumber") String accountNumber,
        @Param("agencyNumber") String agencyNumber
    );
}


