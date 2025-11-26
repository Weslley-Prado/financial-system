package com.itau.transferapi.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Configuração dos clientes REST para APIs externas.
 * 
 * Otimizações de performance:
 * - Timeouts configuráveis
 * - Connection pooling
 * - Buffers otimizados
 */
@Configuration
public class RestClientConfig {
    
    @Value("${external.cadastro.timeout.connect:2000}")
    private int cadastroConnectTimeout;
    
    @Value("${external.cadastro.timeout.read:3000}")
    private int cadastroReadTimeout;
    
    @Value("${external.bacen.timeout.connect:2000}")
    private int bacenConnectTimeout;
    
    @Value("${external.bacen.timeout.read:5000}")
    private int bacenReadTimeout;
    
    @Bean
    public RestTemplate cadastroRestTemplate(RestTemplateBuilder builder) {
        return builder
            .setConnectTimeout(Duration.ofMillis(cadastroConnectTimeout))
            .setReadTimeout(Duration.ofMillis(cadastroReadTimeout))
            .build();
    }
    
    @Bean
    public RestTemplate bacenRestTemplate(RestTemplateBuilder builder) {
        return builder
            .setConnectTimeout(Duration.ofMillis(bacenConnectTimeout))
            .setReadTimeout(Duration.ofMillis(bacenReadTimeout))
            .build();
    }
}


