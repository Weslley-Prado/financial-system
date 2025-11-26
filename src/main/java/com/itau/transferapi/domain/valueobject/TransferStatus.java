package com.itau.transferapi.domain.valueobject;

import java.util.Set;

/**
 * Enumeração que representa os possíveis status de uma transferência.
 * 
 * Máquina de estados:
 * PENDING -> PROCESSING -> COMPLETED -> BACEN_PENDING -> BACEN_NOTIFIED
 *                      -> FAILED
 */
public enum TransferStatus {
    
    /**
     * Transferência criada, aguardando processamento.
     */
    PENDING("Pendente", Set.of()),
    
    /**
     * Transferência em processamento.
     */
    PROCESSING("Em Processamento", Set.of()),
    
    /**
     * Transferência concluída com sucesso.
     */
    COMPLETED("Concluída", Set.of()),
    
    /**
     * Transferência falhou.
     */
    FAILED("Falha", Set.of()),
    
    /**
     * Aguardando notificação ao BACEN.
     */
    BACEN_PENDING("Aguardando BACEN", Set.of()),
    
    /**
     * BACEN notificado com sucesso.
     */
    BACEN_NOTIFIED("BACEN Notificado", Set.of());
    
    private final String description;
    private final Set<TransferStatus> allowedPreviousStates;
    
    TransferStatus(String description, Set<TransferStatus> allowedPreviousStates) {
        this.description = description;
        this.allowedPreviousStates = allowedPreviousStates;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Verifica se é possível transicionar para outro status.
     * 
     * @param newStatus novo status desejado
     * @return true se a transição é permitida
     */
    public boolean canTransitionTo(TransferStatus newStatus) {
        return switch (this) {
            case PENDING -> newStatus == PROCESSING || newStatus == FAILED;
            case PROCESSING -> newStatus == COMPLETED || newStatus == FAILED;
            case COMPLETED -> newStatus == BACEN_PENDING;
            case BACEN_PENDING -> newStatus == BACEN_NOTIFIED || newStatus == FAILED;
            case FAILED, BACEN_NOTIFIED -> false;
        };
    }
    
    /**
     * Verifica se é um status final.
     * 
     * @return true se é status final
     */
    public boolean isFinal() {
        return this == BACEN_NOTIFIED || this == FAILED;
    }
    
    /**
     * Verifica se é um status de sucesso.
     * 
     * @return true se é sucesso
     */
    public boolean isSuccess() {
        return this == COMPLETED || this == BACEN_NOTIFIED;
    }
}


