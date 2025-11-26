package com.itau.transferapi.infrastructure.config;

import com.itau.transferapi.infrastructure.entity.AccountJpaEntity;
import com.itau.transferapi.infrastructure.entity.AccountJpaEntity.AccountStatusJpa;
import com.itau.transferapi.infrastructure.adapter.output.persistence.repository.AccountJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Inicializador de dados para desenvolvimento e testes.
 * 
 * Cria contas de teste automaticamente no startup.
 */
@Slf4j
@Component
@Profile({"local", "test"})
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    
    private final AccountJpaRepository accountRepository;
    
    @Override
    public void run(String... args) {
        log.info("Inicializando dados de teste...");
        
        if (accountRepository.count() == 0) {
            createTestAccounts();
            log.info("Dados de teste criados com sucesso!");
        } else {
            log.info("Dados já existentes, pulando inicialização.");
        }
    }
    
    private void createTestAccounts() {
        // Conta 1 - João Silva
        AccountJpaEntity account1 = AccountJpaEntity.builder()
            .id(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"))
            .accountNumber("12345-6")
            .agencyNumber("0001")
            .clientId(UUID.fromString("11111111-1111-1111-1111-111111111111"))
            .balance(new BigDecimal("5000.00"))
            .availableLimit(new BigDecimal("10000.00"))
            .status(AccountStatusJpa.ACTIVE)
            .build();
        
        // Conta 2 - Maria Santos
        AccountJpaEntity account2 = AccountJpaEntity.builder()
            .id(UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"))
            .accountNumber("98765-4")
            .agencyNumber("0002")
            .clientId(UUID.fromString("22222222-2222-2222-2222-222222222222"))
            .balance(new BigDecimal("3000.00"))
            .availableLimit(new BigDecimal("5000.00"))
            .status(AccountStatusJpa.ACTIVE)
            .build();
        
        // Conta 3 - Carlos Oliveira (INATIVA para testes de validação)
        AccountJpaEntity account3 = AccountJpaEntity.builder()
            .id(UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc"))
            .accountNumber("11111-1")
            .agencyNumber("0001")
            .clientId(UUID.fromString("33333333-3333-3333-3333-333333333333"))
            .balance(new BigDecimal("1000.00"))
            .availableLimit(new BigDecimal("2000.00"))
            .status(AccountStatusJpa.INACTIVE)
            .build();
        
        // Conta 4 - Para testes de limite
        AccountJpaEntity account4 = AccountJpaEntity.builder()
            .id(UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd"))
            .accountNumber("22222-2")
            .agencyNumber("0001")
            .clientId(UUID.fromString("11111111-1111-1111-1111-111111111111"))
            .balance(new BigDecimal("100.00"))
            .availableLimit(new BigDecimal("50.00"))
            .status(AccountStatusJpa.ACTIVE)
            .build();
        
        accountRepository.save(account1);
        accountRepository.save(account2);
        accountRepository.save(account3);
        accountRepository.save(account4);
        
        log.info("Criadas 4 contas de teste");
    }
}


