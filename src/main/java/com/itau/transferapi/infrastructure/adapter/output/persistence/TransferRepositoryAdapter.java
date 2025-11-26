package com.itau.transferapi.infrastructure.adapter.output.persistence;

import com.itau.transferapi.domain.entity.Transfer;
import com.itau.transferapi.domain.repository.TransferRepository;
import com.itau.transferapi.domain.valueobject.AccountId;
import com.itau.transferapi.domain.valueobject.Money;
import com.itau.transferapi.domain.valueobject.TransferId;
import com.itau.transferapi.domain.valueobject.TransferStatus;
import com.itau.transferapi.infrastructure.adapter.output.persistence.repository.TransferJpaRepository;
import com.itau.transferapi.infrastructure.entity.TransferJpaEntity;
import com.itau.transferapi.infrastructure.entity.TransferJpaEntity.TransferStatusJpa;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adapter que implementa o repositório de Transfer usando JPA.
 */
@Component
@RequiredArgsConstructor
public class TransferRepositoryAdapter implements TransferRepository {
    
    private final TransferJpaRepository jpaRepository;
    
    @Override
    public Optional<Transfer> findById(TransferId transferId) {
        return jpaRepository.findById(transferId.value())
            .map(this::toDomain);
    }
    
    @Override
    public Transfer save(Transfer transfer) {
        // Busca entidade existente para evitar conflito de sessão
        TransferJpaEntity entity = jpaRepository.findById(transfer.getId().value())
            .map(existing -> {
                existing.setStatus(mapStatus(transfer.getStatus()));
                existing.setFailureReason(transfer.getFailureReason());
                existing.setBacenNotificationId(transfer.getBacenNotificationId());
                existing.setCompletedAt(transfer.getCompletedAt());
                existing.setBacenNotifiedAt(transfer.getBacenNotifiedAt());
                existing.setBacenRetryCount(transfer.getBacenRetryCount());
                return existing;
            })
            .orElseGet(() -> toEntity(transfer));
        
        TransferJpaEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }
    
    @Override
    public List<Transfer> findBySourceAccountIdAndDateRange(
            AccountId sourceAccountId, 
            LocalDateTime startDate, 
            LocalDateTime endDate) {
        return jpaRepository.findBySourceAccountIdAndCreatedAtBetween(
                sourceAccountId.value(), startDate, endDate)
            .stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<Transfer> findByStatusAndRetryCountLessThan(TransferStatus status, int maxRetries) {
        return jpaRepository.findByStatusAndBacenRetryCountLessThan(
                mapStatus(status), maxRetries)
            .stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<Transfer> findByAccountIdAndDateRange(
            AccountId accountId, 
            LocalDateTime startDate, 
            LocalDateTime endDate) {
        return jpaRepository.findByAccountIdAndDateRange(
                accountId.value(), startDate, endDate)
            .stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }
    
    private Transfer toDomain(TransferJpaEntity entity) {
        return Transfer.builder()
            .id(TransferId.of(entity.getId()))
            .sourceAccountId(AccountId.of(entity.getSourceAccountId()))
            .targetAccountId(AccountId.of(entity.getTargetAccountId()))
            .amount(Money.of(entity.getAmount()))
            .status(mapStatus(entity.getStatus()))
            .failureReason(entity.getFailureReason())
            .bacenNotificationId(entity.getBacenNotificationId())
            .createdAt(entity.getCreatedAt())
            .completedAt(entity.getCompletedAt())
            .bacenNotifiedAt(entity.getBacenNotifiedAt())
            .bacenRetryCount(entity.getBacenRetryCount())
            .build();
    }
    
    private TransferJpaEntity toEntity(Transfer transfer) {
        return TransferJpaEntity.builder()
            .id(transfer.getId().value())
            .sourceAccountId(transfer.getSourceAccountId().value())
            .targetAccountId(transfer.getTargetAccountId().value())
            .amount(transfer.getAmount().getValue())
            .status(mapStatus(transfer.getStatus()))
            .failureReason(transfer.getFailureReason())
            .bacenNotificationId(transfer.getBacenNotificationId())
            .createdAt(transfer.getCreatedAt())
            .completedAt(transfer.getCompletedAt())
            .bacenNotifiedAt(transfer.getBacenNotifiedAt())
            .bacenRetryCount(transfer.getBacenRetryCount())
            .build();
    }
    
    private TransferStatus mapStatus(TransferStatusJpa status) {
        return switch (status) {
            case PENDING -> TransferStatus.PENDING;
            case PROCESSING -> TransferStatus.PROCESSING;
            case COMPLETED -> TransferStatus.COMPLETED;
            case FAILED -> TransferStatus.FAILED;
            case BACEN_PENDING -> TransferStatus.BACEN_PENDING;
            case BACEN_NOTIFIED -> TransferStatus.BACEN_NOTIFIED;
        };
    }
    
    private TransferStatusJpa mapStatus(TransferStatus status) {
        return switch (status) {
            case PENDING -> TransferStatusJpa.PENDING;
            case PROCESSING -> TransferStatusJpa.PROCESSING;
            case COMPLETED -> TransferStatusJpa.COMPLETED;
            case FAILED -> TransferStatusJpa.FAILED;
            case BACEN_PENDING -> TransferStatusJpa.BACEN_PENDING;
            case BACEN_NOTIFIED -> TransferStatusJpa.BACEN_NOTIFIED;
        };
    }
}


