package com.itau.transferapi.infrastructure.adapter.output.client;

import com.itau.transferapi.application.port.output.BacenNotificationPort;
import com.itau.transferapi.domain.entity.Transfer;
import com.itau.transferapi.domain.exception.IntegrationException;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Cliente para integração com API do BACEN.
 * 
 * Implementa padrões de resiliência especiais para o BACEN:
 * - Circuit Breaker
 * - Retry com backoff exponencial
 * - Rate Limiter (respeitando limites do BACEN)
 * - Bulkhead para limitar concorrência
 * - Time Limiter
 * 
 * Em caso de rate limit (HTTP 429), a transferência é marcada
 * como BACEN_PENDING para retry assíncrono posterior.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BacenApiClient implements BacenNotificationPort {
    
    private final RestTemplate bacenRestTemplate;
    
    @Value("${external.bacen.base-url}")
    private String baseUrl;
    
    @Override
    @CircuitBreaker(name = "bacenApi", fallbackMethod = "notifyTransferFallback")
    @Retry(name = "bacenApi")
    @RateLimiter(name = "bacenApi")
    @Bulkhead(name = "bacenApi")
    public String notifyTransfer(Transfer transfer) {
        log.info("Notificando BACEN sobre transferência: {}", transfer.getId());
        
        try {
            String url = baseUrl + "/api/v1/notifications";
            
            BacenNotificationRequest request = new BacenNotificationRequest(
                transfer.getId().value(),
                transfer.getSourceAccountId().value(),
                transfer.getTargetAccountId().value(),
                transfer.getAmount().getValue(),
                transfer.getCreatedAt()
            );
            
            BacenNotificationResponse response = bacenRestTemplate.postForObject(
                url, 
                request, 
                BacenNotificationResponse.class
            );
            
            if (response == null) {
                throw IntegrationException.bacenError("Resposta vazia do BACEN", null);
            }
            
            log.info("BACEN notificado com sucesso: notificationId={}", response.notificationId());
            return response.notificationId();
            
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                log.warn("Rate limit do BACEN atingido para transferência: {}", transfer.getId());
                throw IntegrationException.bacenRateLimit();
            }
            throw IntegrationException.bacenError("Erro ao notificar BACEN: " + e.getMessage(), e);
        }
    }
    
    @Override
    @CircuitBreaker(name = "bacenApi")
    @Retry(name = "bacenApi")
    public NotificationStatus checkNotificationStatus(String notificationId) {
        log.debug("Verificando status da notificação: {}", notificationId);
        
        try {
            String url = baseUrl + "/api/v1/notifications/" + notificationId + "/status";
            BacenStatusResponse response = bacenRestTemplate.getForObject(url, BacenStatusResponse.class);
            
            if (response == null) {
                return NotificationStatus.NOT_FOUND;
            }
            
            return switch (response.status()) {
                case "PENDING" -> NotificationStatus.PENDING;
                case "CONFIRMED" -> NotificationStatus.CONFIRMED;
                case "REJECTED" -> NotificationStatus.REJECTED;
                default -> NotificationStatus.NOT_FOUND;
            };
            
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return NotificationStatus.NOT_FOUND;
            }
            throw IntegrationException.bacenError("Erro ao consultar status: " + e.getMessage(), e);
        }
    }
    
    @SuppressWarnings("unused")
    private String notifyTransferFallback(Transfer transfer, Throwable t) {
        log.error("Fallback ativado para notificação BACEN da transferência {}: {}", 
            transfer.getId(), t.getMessage());
        throw IntegrationException.bacenUnavailable(t);
    }
    
    /**
     * DTO para requisição de notificação ao BACEN.
     */
    private record BacenNotificationRequest(
        UUID transferId,
        UUID sourceAccountId,
        UUID targetAccountId,
        BigDecimal amount,
        LocalDateTime transactionDate
    ) {}
    
    /**
     * DTO para resposta de notificação do BACEN.
     */
    private record BacenNotificationResponse(
        String notificationId,
        String status,
        LocalDateTime processedAt
    ) {}
    
    /**
     * DTO para resposta de status do BACEN.
     */
    private record BacenStatusResponse(
        String notificationId,
        String status
    ) {}
}


