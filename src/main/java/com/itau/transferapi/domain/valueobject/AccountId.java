package com.itau.transferapi.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object imutável que representa o identificador único de uma conta.
 * 
 * Encapsula a lógica de identificação da conta, permitindo
 * validações e formatações centralizadas.
 */
public record AccountId(UUID value) {
    
    public AccountId {
        Objects.requireNonNull(value, "ID da conta não pode ser nulo");
    }
    
    /**
     * Cria um AccountId a partir de um UUID.
     * 
     * @param uuid identificador
     * @return AccountId
     */
    public static AccountId of(UUID uuid) {
        return new AccountId(uuid);
    }
    
    /**
     * Cria um AccountId a partir de uma string.
     * 
     * @param uuid string do UUID
     * @return AccountId
     */
    public static AccountId of(String uuid) {
        return new AccountId(UUID.fromString(uuid));
    }
    
    /**
     * Gera um novo AccountId aleatório.
     * 
     * @return novo AccountId
     */
    public static AccountId generate() {
        return new AccountId(UUID.randomUUID());
    }
    
    @Override
    public String toString() {
        return value.toString();
    }
}


