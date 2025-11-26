package com.itau.transferapi.application.usecase;

import com.itau.transferapi.application.dto.response.BalanceResponse;
import com.itau.transferapi.application.port.input.BalanceQueryUseCase;
import com.itau.transferapi.application.port.output.ClientDataPort;
import com.itau.transferapi.domain.entity.Account;
import com.itau.transferapi.domain.entity.Client;
import com.itau.transferapi.domain.entity.DailyTransferLimit;
import com.itau.transferapi.domain.exception.ResourceNotFoundException;
import com.itau.transferapi.domain.repository.AccountRepository;
import com.itau.transferapi.domain.repository.DailyTransferLimitRepository;
import com.itau.transferapi.domain.valueobject.Money;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Implementação do caso de uso de Consulta de Saldo.
 * 
 * Características:
 * - Cache para alta performance
 * - Circuit Breaker para resiliência
 * - Consulta otimizada
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BalanceQueryUseCaseImpl implements BalanceQueryUseCase {
    
    private final AccountRepository accountRepository;
    private final DailyTransferLimitRepository dailyTransferLimitRepository;
    private final ClientDataPort clientDataPort;
    
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "balances", key = "#accountNumber + '-' + #agencyNumber", unless = "#result == null")
    public BalanceResponse getBalance(String accountNumber, String agencyNumber) {
        log.debug("Consultando saldo: conta={}, agência={}", accountNumber, agencyNumber);
        
        Account account = accountRepository.findByAccountAndAgency(accountNumber, agencyNumber)
            .orElseThrow(() -> ResourceNotFoundException.account(accountNumber));
        
        // Validar que a conta está ativa
        account.validateActive();
        
        // Buscar nome do cliente
        String holderName = getClientName(account);
        
        // Calcular limite diário disponível
        Money dailyLimitAvailable = calculateDailyLimitAvailable(account);
        
        return BalanceResponse.builder()
            .accountNumber(account.getAccountNumber())
            .agencyNumber(account.getAgencyNumber())
            .holderName(holderName)
            .balance(account.getBalance().getValue())
            .formattedBalance(account.getBalance().getFormattedValue())
            .availableLimit(account.getAvailableLimit().getValue())
            .formattedAvailableLimit(account.getAvailableLimit().getFormattedValue())
            .dailyTransferLimitAvailable(dailyLimitAvailable.getValue())
            .formattedDailyTransferLimit(dailyLimitAvailable.getFormattedValue())
            .queryTime(LocalDateTime.now())
            .build();
    }
    
    @CircuitBreaker(name = "cadastroApi", fallbackMethod = "getClientNameFallback")
    @Retry(name = "cadastroApi")
    private String getClientName(Account account) {
        return clientDataPort.findClientById(account.getClientId())
            .map(Client::getName)
            .orElse("Cliente");
    }
    
    @SuppressWarnings("unused")
    private String getClientNameFallback(Account account, Throwable t) {
        log.warn("Fallback para nome do cliente: {}", t.getMessage());
        return "Cliente";
    }
    
    private Money calculateDailyLimitAvailable(Account account) {
        return dailyTransferLimitRepository
            .findByAccountIdAndDate(account.getId(), LocalDate.now())
            .map(DailyTransferLimit::getAvailableLimit)
            .orElse(Money.of("1000.00")); // Limite padrão
    }
}


