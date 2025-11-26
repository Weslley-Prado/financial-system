package com.itau.transferapi.infrastructure.adapter.output.client;

import com.itau.transferapi.application.port.output.ClientDataPort;
import com.itau.transferapi.domain.entity.Client;
import com.itau.transferapi.domain.exception.IntegrationException;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.UUID;

/**
 * Cliente para integração com API de Cadastro.
 * 
 * Implementa padrões de resiliência:
 * - Circuit Breaker
 * - Retry com backoff exponencial
 * - Bulkhead para limitar concorrência
 * - Time Limiter
 * - Cache para reduzir chamadas
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CadastroApiClient implements ClientDataPort {
    
    private final RestTemplate cadastroRestTemplate;
    
    @Value("${external.cadastro.base-url}")
    private String baseUrl;
    
    @Override
    @Cacheable(value = "clients", key = "#clientId", unless = "#result == null")
    @CircuitBreaker(name = "cadastroApi", fallbackMethod = "findClientByIdFallback")
    @Retry(name = "cadastroApi")
    @Bulkhead(name = "cadastroApi")
    public Optional<Client> findClientById(UUID clientId) {
        log.debug("Buscando cliente na API de Cadastro: {}", clientId);
        
        try {
            String url = baseUrl + "/api/v1/clients/" + clientId;
            ClientResponse response = cadastroRestTemplate.getForObject(url, ClientResponse.class);
            
            if (response == null) {
                return Optional.empty();
            }
            
            return Optional.of(Client.builder()
                .id(response.id())
                .name(response.name())
                .documentNumber(response.documentNumber())
                .active(response.active())
                .build());
                
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return Optional.empty();
            }
            throw IntegrationException.cadastroError("Erro ao buscar cliente: " + e.getMessage(), e);
        }
    }
    
    @Override
    @Cacheable(value = "clientsByDocument", key = "#documentNumber", unless = "#result == null")
    @CircuitBreaker(name = "cadastroApi", fallbackMethod = "findClientByDocumentFallback")
    @Retry(name = "cadastroApi")
    @Bulkhead(name = "cadastroApi")
    public Optional<Client> findClientByDocument(String documentNumber) {
        log.debug("Buscando cliente por documento: {}", documentNumber);
        
        try {
            String url = baseUrl + "/api/v1/clients/document/" + documentNumber;
            ClientResponse response = cadastroRestTemplate.getForObject(url, ClientResponse.class);
            
            if (response == null) {
                return Optional.empty();
            }
            
            return Optional.of(Client.builder()
                .id(response.id())
                .name(response.name())
                .documentNumber(response.documentNumber())
                .active(response.active())
                .build());
                
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return Optional.empty();
            }
            throw IntegrationException.cadastroError("Erro ao buscar cliente: " + e.getMessage(), e);
        }
    }
    
    @SuppressWarnings("unused")
    private Optional<Client> findClientByIdFallback(UUID clientId, Throwable t) {
        log.warn("Fallback ativado para busca de cliente {}: {}", clientId, t.getMessage());
        // Retorna um cliente padrão em caso de falha
        // Decisão de negócio: priorizar disponibilidade
        return Optional.of(Client.builder()
            .id(clientId)
            .name("Cliente")
            .documentNumber("***")
            .active(true)
            .build());
    }
    
    @SuppressWarnings("unused")
    private Optional<Client> findClientByDocumentFallback(String documentNumber, Throwable t) {
        log.warn("Fallback ativado para busca de cliente por documento: {}", t.getMessage());
        return Optional.empty();
    }
    
    /**
     * DTO para resposta da API de Cadastro.
     */
    private record ClientResponse(
        UUID id,
        String name,
        String documentNumber,
        boolean active
    ) {}
}


