package com.itau.transferapi.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object imutável que representa o identificador único de uma transferência.
 */
public record TransferId(UUID value) {
    
    public TransferId {
        Objects.requireNonNull(value, "ID da transferência não pode ser nulo");
    }
    
    /**
     * Cria um TransferId a partir de um UUID.
     * 
     * @param uuid identificador
     * @return TransferId
     */
    public static TransferId of(UUID uuid) {
        return new TransferId(uuid);
    }
    
    /**
     * Cria um TransferId a partir de uma string.
     * 
     * @param uuid string do UUID
     * @return TransferId
     */
    public static TransferId of(String uuid) {
        return new TransferId(UUID.fromString(uuid));
    }
    
    /**
     * Gera um novo TransferId aleatório.
     * 
     * @return novo TransferId
     */
    public static TransferId generate() {
        return new TransferId(UUID.randomUUID());
    }
    
    @Override
    public String toString() {
        return value.toString();
    }
}


