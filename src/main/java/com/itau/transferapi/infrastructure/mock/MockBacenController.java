package com.itau.transferapi.infrastructure.mock;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Mock da API do BACEN para desenvolvimento e testes.
 * 
 * Este controller simula a API do Banco Central,
 * incluindo comportamento de rate limit (HTTP 429).
 * 
 * Características simuladas:
 * - Rate limit de 10% das requisições
 * - Latência variável
 * - Armazenamento de notificações
 * 
 * Ativo apenas nos perfis: local, test
 */
@Slf4j
@RestController
@RequestMapping("/mock/bacen/api/v1/notifications")
@Profile({"local", "test"})
@Hidden
public class MockBacenController {
    
    private final Map<String, NotificationData> notifications = new ConcurrentHashMap<>();
    private final AtomicInteger requestCounter = new AtomicInteger(0);
    private final Random random = new Random();
    
    // Simula rate limit em 10% das requisições
    private static final int RATE_LIMIT_PERCENTAGE = 10;
    
    @PostMapping
    public ResponseEntity<NotificationResponse> createNotification(
            @RequestBody NotificationRequest request) {
        
        int requestNumber = requestCounter.incrementAndGet();
        log.debug("[MOCK BACEN] Recebida notificação #{}: transferId={}", 
            requestNumber, request.transferId());
        
        // Simula rate limit
        if (shouldRateLimit()) {
            log.warn("[MOCK BACEN] Rate limit atingido para requisição #{}", requestNumber);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
        
        // Simula latência variável (10-50ms)
        simulateLatency();
        
        String notificationId = "BCN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        NotificationData data = new NotificationData(
            notificationId,
            request.transferId(),
            request.sourceAccountId(),
            request.targetAccountId(),
            request.amount(),
            "CONFIRMED",
            LocalDateTime.now()
        );
        
        notifications.put(notificationId, data);
        
        log.info("[MOCK BACEN] Notificação criada: {} para transferência {}", 
            notificationId, request.transferId());
        
        return ResponseEntity.ok(new NotificationResponse(
            notificationId,
            "CONFIRMED",
            LocalDateTime.now()
        ));
    }
    
    @GetMapping("/{notificationId}/status")
    public ResponseEntity<StatusResponse> getStatus(@PathVariable String notificationId) {
        log.debug("[MOCK BACEN] Consultando status: {}", notificationId);
        
        NotificationData data = notifications.get(notificationId);
        if (data == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(new StatusResponse(notificationId, data.status()));
    }
    
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(Map.of(
            "totalNotifications", notifications.size(),
            "totalRequests", requestCounter.get()
        ));
    }
    
    private boolean shouldRateLimit() {
        return random.nextInt(100) < RATE_LIMIT_PERCENTAGE;
    }
    
    private void simulateLatency() {
        try {
            Thread.sleep(10 + random.nextInt(40));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    public record NotificationRequest(
        UUID transferId,
        UUID sourceAccountId,
        UUID targetAccountId,
        BigDecimal amount,
        LocalDateTime transactionDate
    ) {}
    
    public record NotificationResponse(
        String notificationId,
        String status,
        LocalDateTime processedAt
    ) {}
    
    public record StatusResponse(
        String notificationId,
        String status
    ) {}
    
    private record NotificationData(
        String notificationId,
        UUID transferId,
        UUID sourceAccountId,
        UUID targetAccountId,
        BigDecimal amount,
        String status,
        LocalDateTime processedAt
    ) {}
}


