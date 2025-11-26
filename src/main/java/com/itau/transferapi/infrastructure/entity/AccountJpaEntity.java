package com.itau.transferapi.infrastructure.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidade JPA para persistência de Conta Corrente.
 */
@Entity
@Table(name = "accounts", indexes = {
    @Index(name = "idx_account_number_agency", columnList = "account_number, agency_number"),
    @Index(name = "idx_client_id", columnList = "client_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountJpaEntity {
    
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
    
    @Column(name = "account_number", nullable = false, length = 10)
    private String accountNumber;
    
    @Column(name = "agency_number", nullable = false, length = 6)
    private String agencyNumber;
    
    @Column(name = "client_id", nullable = false)
    private UUID clientId;
    
    @Column(name = "balance", nullable = false, precision = 15, scale = 2)
    private BigDecimal balance;
    
    @Column(name = "available_limit", nullable = false, precision = 15, scale = 2)
    private BigDecimal availableLimit;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AccountStatusJpa status;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Version
    @Column(name = "version")
    private Long version;
    
    public enum AccountStatusJpa {
        ACTIVE, INACTIVE, BLOCKED, CLOSED
    }
}


