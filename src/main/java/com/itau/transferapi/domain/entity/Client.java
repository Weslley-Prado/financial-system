package com.itau.transferapi.domain.entity;

import lombok.Builder;
import lombok.Getter;

import java.util.Objects;
import java.util.UUID;

/**
 * Entidade de domínio que representa um Cliente.
 * 
 * Esta entidade é principalmente utilizada para cache de dados
 * vindos da API de Cadastro externa.
 */
@Getter
@Builder
public class Client {
    
    private final UUID id;
    private final String name;
    private final String documentNumber;
    private final boolean active;
    
    /**
     * Verifica se o cliente está ativo.
     * 
     * @return true se o cliente está ativo
     */
    public boolean isActive() {
        return this.active;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Client client = (Client) o;
        return Objects.equals(id, client.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}


