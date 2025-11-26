package com.itau.transferapi.infrastructure.adapter.output.persistence;

import com.itau.transferapi.domain.entity.DailyTransferLimit;
import com.itau.transferapi.domain.repository.DailyTransferLimitRepository;
import com.itau.transferapi.domain.valueobject.AccountId;
import com.itau.transferapi.domain.valueobject.Money;
import com.itau.transferapi.infrastructure.adapter.output.persistence.repository.DailyTransferLimitJpaRepository;
import com.itau.transferapi.infrastructure.entity.DailyTransferLimitJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Adapter que implementa o repositório de DailyTransferLimit usando JPA.
 */
@Component
@RequiredArgsConstructor
public class DailyTransferLimitRepositoryAdapter implements DailyTransferLimitRepository {
    
    private final DailyTransferLimitJpaRepository jpaRepository;
    
    @Override
    public Optional<DailyTransferLimit> findByAccountIdAndDate(AccountId accountId, LocalDate date) {
        return jpaRepository.findByAccountIdAndDate(accountId.value(), date)
            .map(this::toDomain);
    }
    
    @Override
    public DailyTransferLimit save(DailyTransferLimit dailyLimit) {
        // Busca entidade existente para evitar conflito de sessão
        DailyTransferLimitJpaEntity entity = jpaRepository
            .findByAccountIdAndDate(dailyLimit.getAccountId().value(), dailyLimit.getDate())
            .map(existing -> {
                existing.setUsedAmount(dailyLimit.getUsedAmount().getValue());
                existing.setDailyLimit(dailyLimit.getDailyLimit().getValue());
                return existing;
            })
            .orElseGet(() -> toEntity(dailyLimit));
        
        DailyTransferLimitJpaEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }
    
    @Override
    public Optional<DailyTransferLimit> findByAccountIdAndDateForUpdate(AccountId accountId, LocalDate date) {
        return jpaRepository.findByAccountIdAndDateForUpdate(accountId.value(), date)
            .map(this::toDomain);
    }
    
    private DailyTransferLimit toDomain(DailyTransferLimitJpaEntity entity) {
        return DailyTransferLimit.builder()
            .accountId(AccountId.of(entity.getAccountId()))
            .date(entity.getDate())
            .usedAmount(Money.of(entity.getUsedAmount()))
            .dailyLimit(Money.of(entity.getDailyLimit()))
            .build();
    }
    
    private DailyTransferLimitJpaEntity toEntity(DailyTransferLimit dailyLimit) {
        return DailyTransferLimitJpaEntity.builder()
            .accountId(dailyLimit.getAccountId().value())
            .date(dailyLimit.getDate())
            .usedAmount(dailyLimit.getUsedAmount().getValue())
            .dailyLimit(dailyLimit.getDailyLimit().getValue())
            .build();
    }
}


