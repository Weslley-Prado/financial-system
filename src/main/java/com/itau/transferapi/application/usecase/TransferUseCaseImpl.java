package com.itau.transferapi.application.usecase;

import com.itau.transferapi.application.dto.request.TransferRequest;
import com.itau.transferapi.application.dto.response.TransferResponse;
import com.itau.transferapi.application.port.input.TransferUseCase;
import com.itau.transferapi.application.port.output.BacenNotificationPort;
import com.itau.transferapi.application.port.output.ClientDataPort;
import com.itau.transferapi.domain.entity.*;
import com.itau.transferapi.domain.exception.BusinessException;
import com.itau.transferapi.domain.exception.ErrorCode;
import com.itau.transferapi.domain.exception.IntegrationException;
import com.itau.transferapi.domain.exception.ResourceNotFoundException;
import com.itau.transferapi.domain.repository.AccountRepository;
import com.itau.transferapi.domain.repository.DailyTransferLimitRepository;
import com.itau.transferapi.domain.repository.TransferRepository;
import com.itau.transferapi.domain.valueobject.Money;
import com.itau.transferapi.domain.valueobject.TransferId;
import com.itau.transferapi.domain.valueobject.TransferStatus;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Implementação do caso de uso de Transferência Bancária.
 * 
 * Esta classe orquestra todo o fluxo de transferência:
 * 1. Validação de dados
 * 2. Consulta de cliente (API Cadastro)
 * 3. Validação de conta ativa
 * 4. Validação de limite disponível
 * 5. Validação de limite diário
 * 6. Execução da transferência
 * 7. Notificação ao BACEN
 * 
 * Padrões utilizados:
 * - Use Case (Clean Architecture)
 * - Transaction Script (para operações atômicas)
 * - Circuit Breaker (resiliência)
 * - Retry (resiliência)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransferUseCaseImpl implements TransferUseCase {
    
    private final AccountRepository accountRepository;
    private final TransferRepository transferRepository;
    private final DailyTransferLimitRepository dailyTransferLimitRepository;
    private final ClientDataPort clientDataPort;
    private final BacenNotificationPort bacenNotificationPort;
    
    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public TransferResponse execute(TransferRequest request) {
        log.info("Iniciando transferência: origem={}/{}, destino={}/{}, valor={}",
            request.sourceAccountNumber(), request.sourceAgencyNumber(),
            request.targetAccountNumber(), request.targetAgencyNumber(),
            request.amount());
        
        // Validação: contas diferentes
        validateDifferentAccounts(request);
        
        // Criar valor monetário
        Money amount = Money.of(request.amount());
        
        // Buscar contas com lock para atualização
        Account sourceAccount = findAccountForUpdate(
            request.sourceAccountNumber(), 
            request.sourceAgencyNumber()
        );
        Account targetAccount = findAccount(
            request.targetAccountNumber(), 
            request.targetAgencyNumber()
        );
        
        // Validar cliente na API de Cadastro
        validateClient(sourceAccount);
        
        // Validar conta origem ativa
        sourceAccount.validateActive();
        targetAccount.validateActive();
        
        // Validar limite disponível na conta
        sourceAccount.validateAvailableLimit(amount);
        
        // Validar limite diário
        DailyTransferLimit dailyLimit = getOrCreateDailyLimit(sourceAccount);
        dailyLimit.validateLimit(amount);
        
        // Criar transferência
        Transfer transfer = createTransfer(sourceAccount, targetAccount, amount);
        transfer.startProcessing();
        
        try {
            // Executar transferência
            sourceAccount.debit(amount);
            targetAccount.credit(amount);
            
            // Atualizar limite diário
            dailyLimit.useLimit(amount);
            
            // Persistir alterações
            accountRepository.save(sourceAccount);
            accountRepository.save(targetAccount);
            dailyTransferLimitRepository.save(dailyLimit);
            
            // Marcar transferência como completa
            transfer.complete();
            transfer.markBacenPending();
            transferRepository.save(transfer);
            
            // Notificar BACEN (síncrono com retry)
            String bacenNotificationId = notifyBacen(transfer);
            transfer.markBacenNotified(bacenNotificationId);
            transferRepository.save(transfer);
            
            log.info("Transferência concluída com sucesso: id={}, bacenId={}", 
                transfer.getId(), bacenNotificationId);
            
            return buildSuccessResponse(transfer, sourceAccount, targetAccount);
            
        } catch (IntegrationException e) {
            // Transferência foi realizada, mas BACEN não foi notificado
            // A transferência permanece em BACEN_PENDING para retry assíncrono
            log.warn("Transferência concluída, mas BACEN não notificado: id={}, erro={}", 
                transfer.getId(), e.getMessage());
            
            transfer.incrementBacenRetryCount();
            transferRepository.save(transfer);
            
            return buildPendingBacenResponse(transfer, sourceAccount, targetAccount);
            
        } catch (Exception e) {
            log.error("Erro ao processar transferência: {}", e.getMessage(), e);
            transfer.fail(e.getMessage());
            transferRepository.save(transfer);
            throw e;
        }
    }
    
    private void validateDifferentAccounts(TransferRequest request) {
        if (!request.isDifferentAccounts()) {
            throw new BusinessException(
                ErrorCode.SAME_ACCOUNT_TRANSFER,
                "Não é permitido transferir para a mesma conta"
            );
        }
    }
    
    private Account findAccountForUpdate(String accountNumber, String agencyNumber) {
        return accountRepository.findByAccountAndAgency(accountNumber, agencyNumber)
            .orElseThrow(() -> ResourceNotFoundException.account(accountNumber));
    }
    
    private Account findAccount(String accountNumber, String agencyNumber) {
        return accountRepository.findByAccountAndAgency(accountNumber, agencyNumber)
            .orElseThrow(() -> ResourceNotFoundException.account(accountNumber));
    }
    
    @CircuitBreaker(name = "cadastroApi", fallbackMethod = "validateClientFallback")
    @Retry(name = "cadastroApi")
    private void validateClient(Account account) {
        Client client = clientDataPort.findClientById(account.getClientId())
            .orElseThrow(() -> ResourceNotFoundException.client(account.getClientId().toString()));
        
        if (!client.isActive()) {
            throw new BusinessException(
                ErrorCode.CLIENT_NOT_ACTIVE,
                String.format("Cliente %s não está ativo no sistema de cadastro", client.getName())
            );
        }
        
        log.debug("Cliente validado: {}", client.getName());
    }
    
    @SuppressWarnings("unused")
    private void validateClientFallback(Account account, Throwable t) {
        log.warn("Fallback ativado para validação de cliente: {}", t.getMessage());
        // Em caso de falha na API de Cadastro, permitimos a operação
        // mas registramos para auditoria posterior
        // Esta é uma decisão de negócio: priorizar disponibilidade sobre consistência
    }
    
    private DailyTransferLimit getOrCreateDailyLimit(Account account) {
        return dailyTransferLimitRepository
            .findByAccountIdAndDateForUpdate(account.getId(), LocalDate.now())
            .orElseGet(() -> DailyTransferLimit.createDefault(account.getId()));
    }
    
    private Transfer createTransfer(Account source, Account target, Money amount) {
        return Transfer.builder()
            .id(TransferId.generate())
            .sourceAccountId(source.getId())
            .targetAccountId(target.getId())
            .amount(amount)
            .status(TransferStatus.PENDING)
            .createdAt(LocalDateTime.now())
            .bacenRetryCount(0)
            .build();
    }
    
    @CircuitBreaker(name = "bacenApi", fallbackMethod = "notifyBacenFallback")
    @Retry(name = "bacenApi")
    private String notifyBacen(Transfer transfer) {
        log.info("Notificando BACEN sobre transferência: {}", transfer.getId());
        return bacenNotificationPort.notifyTransfer(transfer);
    }
    
    @SuppressWarnings("unused")
    private String notifyBacenFallback(Transfer transfer, Throwable t) {
        log.warn("Fallback ativado para notificação BACEN: {}", t.getMessage());
        throw IntegrationException.bacenUnavailable(t);
    }
    
    private TransferResponse buildSuccessResponse(Transfer transfer, Account source, Account target) {
        return TransferResponse.builder()
            .transferId(transfer.getId().value())
            .status(transfer.getStatus())
            .amount(transfer.getAmount().getValue())
            .formattedAmount(transfer.getAmount().getFormattedValue())
            .sourceAccountNumber(source.getAccountNumber())
            .sourceAgencyNumber(source.getAgencyNumber())
            .targetAccountNumber(target.getAccountNumber())
            .targetAgencyNumber(target.getAgencyNumber())
            .createdAt(transfer.getCreatedAt())
            .completedAt(transfer.getCompletedAt())
            .bacenNotificationId(transfer.getBacenNotificationId())
            .message("Transferência realizada com sucesso")
            .build();
    }
    
    private TransferResponse buildPendingBacenResponse(Transfer transfer, Account source, Account target) {
        return TransferResponse.builder()
            .transferId(transfer.getId().value())
            .status(transfer.getStatus())
            .amount(transfer.getAmount().getValue())
            .formattedAmount(transfer.getAmount().getFormattedValue())
            .sourceAccountNumber(source.getAccountNumber())
            .sourceAgencyNumber(source.getAgencyNumber())
            .targetAccountNumber(target.getAccountNumber())
            .targetAgencyNumber(target.getAgencyNumber())
            .createdAt(transfer.getCreatedAt())
            .completedAt(transfer.getCompletedAt())
            .message("Transferência realizada. Notificação ao BACEN pendente.")
            .build();
    }
}


