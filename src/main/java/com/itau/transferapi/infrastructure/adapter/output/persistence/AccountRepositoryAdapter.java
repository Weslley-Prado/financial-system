package com.itau.transferapi.infrastructure.adapter.output.persistence;

import com.itau.transferapi.domain.entity.Account;
import com.itau.transferapi.domain.repository.AccountRepository;
import com.itau.transferapi.domain.valueobject.AccountId;
import com.itau.transferapi.domain.valueobject.AccountStatus;
import com.itau.transferapi.domain.valueobject.Money;
import com.itau.transferapi.infrastructure.adapter.output.persistence.repository.AccountJpaRepository;
import com.itau.transferapi.infrastructure.entity.AccountJpaEntity;
import com.itau.transferapi.infrastructure.entity.AccountJpaEntity.AccountStatusJpa;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Adapter que implementa o repositório de Account usando JPA.
 */
@Component
@RequiredArgsConstructor
public class AccountRepositoryAdapter implements AccountRepository {
    
    private final AccountJpaRepository jpaRepository;
    
    @Override
    public Optional<Account> findById(AccountId accountId) {
        return jpaRepository.findById(accountId.value())
            .map(this::toDomain);
    }
    
    @Override
    public Optional<Account> findByAccountAndAgency(String accountNumber, String agencyNumber) {
        return jpaRepository.findByAccountNumberAndAgencyNumber(accountNumber, agencyNumber)
            .map(this::toDomain);
    }
    
    @Override
    public Optional<Account> findByClientId(UUID clientId) {
        return jpaRepository.findByClientId(clientId)
            .map(this::toDomain);
    }
    
    @Override
    public Account save(Account account) {
        // Busca entidade existente para evitar conflito de sessão
        AccountJpaEntity entity = jpaRepository.findById(account.getId().value())
            .map(existing -> {
                existing.setBalance(account.getBalance().getValue());
                existing.setAvailableLimit(account.getAvailableLimit().getValue());
                existing.setStatus(mapStatus(account.getStatus()));
                existing.setUpdatedAt(account.getUpdatedAt());
                return existing;
            })
            .orElseGet(() -> toEntity(account));
        
        AccountJpaEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }
    
    @Override
    public boolean existsById(AccountId accountId) {
        return jpaRepository.existsById(accountId.value());
    }
    
    @Override
    public Optional<Account> findByIdForUpdate(AccountId accountId) {
        return jpaRepository.findByIdForUpdate(accountId.value())
            .map(this::toDomain);
    }
    
    private Account toDomain(AccountJpaEntity entity) {
        return Account.builder()
            .id(AccountId.of(entity.getId()))
            .accountNumber(entity.getAccountNumber())
            .agencyNumber(entity.getAgencyNumber())
            .clientId(entity.getClientId())
            .balance(Money.of(entity.getBalance()))
            .availableLimit(Money.of(entity.getAvailableLimit()))
            .status(mapStatus(entity.getStatus()))
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }
    
    private AccountJpaEntity toEntity(Account account) {
        return AccountJpaEntity.builder()
            .id(account.getId().value())
            .accountNumber(account.getAccountNumber())
            .agencyNumber(account.getAgencyNumber())
            .clientId(account.getClientId())
            .balance(account.getBalance().getValue())
            .availableLimit(account.getAvailableLimit().getValue())
            .status(mapStatus(account.getStatus()))
            .createdAt(account.getCreatedAt())
            .updatedAt(account.getUpdatedAt())
            .build();
    }
    
    private AccountStatus mapStatus(AccountStatusJpa status) {
        return switch (status) {
            case ACTIVE -> AccountStatus.ACTIVE;
            case INACTIVE -> AccountStatus.INACTIVE;
            case BLOCKED -> AccountStatus.BLOCKED;
            case CLOSED -> AccountStatus.CLOSED;
        };
    }
    
    private AccountStatusJpa mapStatus(AccountStatus status) {
        return switch (status) {
            case ACTIVE -> AccountStatusJpa.ACTIVE;
            case INACTIVE -> AccountStatusJpa.INACTIVE;
            case BLOCKED -> AccountStatusJpa.BLOCKED;
            case CLOSED -> AccountStatusJpa.CLOSED;
        };
    }
}


