package com.itau.transferapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Aplicação principal da API de Transferências Bancárias.
 * 
 * Case Técnico Itaú - Sistema de alta disponibilidade e resiliência
 * para operações de consulta de saldo e transferência entre contas.
 * 
 * Requisitos atendidos:
 * - 6.000 TPS com latência < 100ms
 * - Alta disponibilidade
 * - Padrões de resiliência (Circuit Breaker, Retry, Rate Limiter)
 * - Integração com BACEN com tratamento de rate limit
 * 
 * @author Itaú Technical Case
 * @version 1.0.0
 */
@SpringBootApplication
@EnableCaching
public class TransferApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(TransferApiApplication.class, args);
    }
}


