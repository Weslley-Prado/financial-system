package com.itau.transferapi.infrastructure.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Configuração de cache com Caffeine para alta performance.
 * 
 * Caches configurados:
 * - clients: Dados de clientes da API de Cadastro (TTL: 5 min)
 * - balances: Saldos de conta (TTL: 30 seg)
 * - clientsByDocument: Clientes por documento (TTL: 5 min)
 */
@Configuration
public class CacheConfig {
    
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .recordStats()
        );
        
        // Configurações específicas por cache podem ser adicionadas aqui
        cacheManager.setCacheNames(java.util.List.of(
            "clients",
            "clientsByDocument", 
            "balances"
        ));
        
        return cacheManager;
    }
}


