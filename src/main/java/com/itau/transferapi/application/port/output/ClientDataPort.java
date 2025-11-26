package com.itau.transferapi.application.port.output;

import com.itau.transferapi.domain.entity.Client;

import java.util.Optional;
import java.util.UUID;

/**
 * Porta de saída para integração com API de Cadastro.
 * 
 * Define o contrato para obtenção de dados de clientes
 * a partir do sistema externo de cadastro.
 */
public interface ClientDataPort {
    
    /**
     * Busca dados de um cliente pelo seu ID.
     * 
     * @param clientId ID do cliente
     * @return Optional contendo o cliente ou vazio
     */
    Optional<Client> findClientById(UUID clientId);
    
    /**
     * Busca dados de um cliente pelo número do documento.
     * 
     * @param documentNumber número do documento (CPF/CNPJ)
     * @return Optional contendo o cliente ou vazio
     */
    Optional<Client> findClientByDocument(String documentNumber);
}


