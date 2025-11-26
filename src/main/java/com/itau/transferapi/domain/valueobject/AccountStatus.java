package com.itau.transferapi.domain.valueobject;

/**
 * Enumeração que representa os possíveis status de uma conta corrente.
 */
public enum AccountStatus {
    
    /**
     * Conta ativa e disponível para operações.
     */
    ACTIVE("Ativa"),
    
    /**
     * Conta inativa, não permite operações.
     */
    INACTIVE("Inativa"),
    
    /**
     * Conta bloqueada por questões de segurança ou compliance.
     */
    BLOCKED("Bloqueada"),
    
    /**
     * Conta encerrada permanentemente.
     */
    CLOSED("Encerrada");
    
    private final String description;
    
    AccountStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Verifica se a conta permite operações.
     * 
     * @return true se permite operações
     */
    public boolean allowsOperations() {
        return this == ACTIVE;
    }
}


