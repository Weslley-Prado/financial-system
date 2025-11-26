package com.itau.transferapi.domain.entity;

import com.itau.transferapi.domain.valueobject.AccountId;
import com.itau.transferapi.domain.valueobject.Money;
import com.itau.transferapi.domain.valueobject.TransferId;
import com.itau.transferapi.domain.valueobject.TransferStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidade de domínio que representa uma Transferência Bancária.
 * 
 * Aggregate Root responsável por:
 * - Manter o estado da transferência
 * - Garantir consistência das operações
 * - Registrar histórico de transições de estado
 * 
 * Estados possíveis:
 * - PENDING: Transferência criada, aguardando processamento
 * - PROCESSING: Transferência em processamento
 * - COMPLETED: Transferência concluída com sucesso
 * - FAILED: Transferência falhou
 * - BACEN_PENDING: Aguardando notificação ao BACEN
 * - BACEN_NOTIFIED: BACEN notificado com sucesso
 */
@Getter
@Builder
public class Transfer {
    
    private final TransferId id;
    private final AccountId sourceAccountId;
    private final AccountId targetAccountId;
    private final Money amount;
    private TransferStatus status;
    private String failureReason;
    private String bacenNotificationId;
    private final LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private LocalDateTime bacenNotifiedAt;
    private int bacenRetryCount;
    
    /**
     * Inicia o processamento da transferência.
     */
    public void startProcessing() {
        validateStatusTransition(TransferStatus.PROCESSING);
        this.status = TransferStatus.PROCESSING;
    }
    
    /**
     * Marca a transferência como concluída com sucesso.
     */
    public void complete() {
        validateStatusTransition(TransferStatus.COMPLETED);
        this.status = TransferStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }
    
    /**
     * Marca a transferência como falha.
     * 
     * @param reason motivo da falha
     */
    public void fail(String reason) {
        this.status = TransferStatus.FAILED;
        this.failureReason = reason;
        this.completedAt = LocalDateTime.now();
    }
    
    /**
     * Marca a transferência como pendente de notificação ao BACEN.
     */
    public void markBacenPending() {
        validateStatusTransition(TransferStatus.BACEN_PENDING);
        this.status = TransferStatus.BACEN_PENDING;
    }
    
    /**
     * Registra a notificação bem-sucedida ao BACEN.
     * 
     * @param notificationId ID da notificação retornado pelo BACEN
     */
    public void markBacenNotified(String notificationId) {
        validateStatusTransition(TransferStatus.BACEN_NOTIFIED);
        this.status = TransferStatus.BACEN_NOTIFIED;
        this.bacenNotificationId = notificationId;
        this.bacenNotifiedAt = LocalDateTime.now();
    }
    
    /**
     * Incrementa o contador de tentativas de notificação ao BACEN.
     */
    public void incrementBacenRetryCount() {
        this.bacenRetryCount++;
    }
    
    /**
     * Verifica se a transferência foi completada com sucesso.
     * 
     * @return true se completada, false caso contrário
     */
    public boolean isCompleted() {
        return TransferStatus.COMPLETED.equals(this.status) ||
               TransferStatus.BACEN_NOTIFIED.equals(this.status);
    }
    
    /**
     * Verifica se a transferência pode ser reprocessada.
     * 
     * @return true se pode ser reprocessada
     */
    public boolean canRetry() {
        return TransferStatus.FAILED.equals(this.status) ||
               TransferStatus.BACEN_PENDING.equals(this.status);
    }
    
    /**
     * Valida se a transição de estado é permitida.
     * 
     * @param newStatus novo status
     * @throws IllegalStateException se a transição não for permitida
     */
    private void validateStatusTransition(TransferStatus newStatus) {
        if (!this.status.canTransitionTo(newStatus)) {
            throw new IllegalStateException(
                String.format("Transição de status inválida: %s -> %s", 
                    this.status, newStatus)
            );
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transfer transfer = (Transfer) o;
        return Objects.equals(id, transfer.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}


