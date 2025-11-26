package com.itau.transferapi.infrastructure.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidade JPA para persistência de Transferência.
 */
@Entity
@Table(name = "transfers", indexes = {
    @Index(name = "idx_source_account", columnList = "source_account_id"),
    @Index(name = "idx_target_account", columnList = "target_account_id"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferJpaEntity {
    
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
    
    @Column(name = "source_account_id", nullable = false)
    private UUID sourceAccountId;
    
    @Column(name = "target_account_id", nullable = false)
    private UUID targetAccountId;
    
    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TransferStatusJpa status;
    
    @Column(name = "failure_reason", length = 500)
    private String failureReason;
    
    @Column(name = "bacen_notification_id", length = 100)
    private String bacenNotificationId;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @Column(name = "bacen_notified_at")
    private LocalDateTime bacenNotifiedAt;
    
    @Column(name = "bacen_retry_count", nullable = false)
    private int bacenRetryCount;
    
    @Version
    @Column(name = "version")
    private Long version;
    
    public enum TransferStatusJpa {
        PENDING, PROCESSING, COMPLETED, FAILED, BACEN_PENDING, BACEN_NOTIFIED
    }
}


