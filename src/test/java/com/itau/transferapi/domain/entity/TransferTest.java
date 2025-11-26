package com.itau.transferapi.domain.entity;

import com.itau.transferapi.domain.valueobject.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Transfer Entity Tests")
class TransferTest {
    
    private AccountId sourceAccountId;
    private AccountId targetAccountId;
    private Money amount;
    
    @BeforeEach
    void setUp() {
        sourceAccountId = AccountId.generate();
        targetAccountId = AccountId.generate();
        amount = Money.of("100.00");
    }
    
    private Transfer createPendingTransfer() {
        return Transfer.builder()
            .id(TransferId.generate())
            .sourceAccountId(sourceAccountId)
            .targetAccountId(targetAccountId)
            .amount(amount)
            .status(TransferStatus.PENDING)
            .createdAt(LocalDateTime.now())
            .bacenRetryCount(0)
            .build();
    }
    
    @Nested
    @DisplayName("Criação de Transfer")
    class CreationTests {
        
        @Test
        @DisplayName("Deve criar transferência com builder")
        void shouldCreateWithBuilder() {
            Transfer transfer = createPendingTransfer();
            
            assertThat(transfer).isNotNull();
            assertThat(transfer.getSourceAccountId()).isEqualTo(sourceAccountId);
            assertThat(transfer.getTargetAccountId()).isEqualTo(targetAccountId);
            assertThat(transfer.getAmount()).isEqualTo(amount);
            assertThat(transfer.getStatus()).isEqualTo(TransferStatus.PENDING);
        }
        
        @Test
        @DisplayName("Deve criar transferência com todos os campos")
        void shouldCreateWithAllFields() {
            TransferId id = TransferId.generate();
            LocalDateTime now = LocalDateTime.now();
            
            Transfer transfer = Transfer.builder()
                .id(id)
                .sourceAccountId(sourceAccountId)
                .targetAccountId(targetAccountId)
                .amount(amount)
                .status(TransferStatus.PENDING)
                .createdAt(now)
                .bacenRetryCount(0)
                .build();
            
            assertThat(transfer.getId()).isEqualTo(id);
            assertThat(transfer.getCreatedAt()).isEqualTo(now);
            assertThat(transfer.getBacenRetryCount()).isEqualTo(0);
        }
    }
    
    @Nested
    @DisplayName("Transições de Status")
    class StatusTransitionTests {
        
        @Test
        @DisplayName("Deve iniciar processamento")
        void shouldStartProcessing() {
            Transfer transfer = createPendingTransfer();
            
            transfer.startProcessing();
            
            assertThat(transfer.getStatus()).isEqualTo(TransferStatus.PROCESSING);
        }
        
        @Test
        @DisplayName("Deve completar transferência")
        void shouldComplete() {
            Transfer transfer = createPendingTransfer();
            transfer.startProcessing();
            
            transfer.complete();
            
            assertThat(transfer.getStatus()).isEqualTo(TransferStatus.COMPLETED);
            assertThat(transfer.getCompletedAt()).isNotNull();
        }
        
        @Test
        @DisplayName("Deve falhar transferência")
        void shouldFail() {
            Transfer transfer = createPendingTransfer();
            transfer.startProcessing();
            
            transfer.fail("Saldo insuficiente");
            
            assertThat(transfer.getStatus()).isEqualTo(TransferStatus.FAILED);
            assertThat(transfer.getFailureReason()).isEqualTo("Saldo insuficiente");
            assertThat(transfer.getCompletedAt()).isNotNull();
        }
        
        @Test
        @DisplayName("Deve marcar como BACEN pending")
        void shouldMarkAsBacenPending() {
            Transfer transfer = createPendingTransfer();
            transfer.startProcessing();
            transfer.complete();
            
            transfer.markBacenPending();
            
            assertThat(transfer.getStatus()).isEqualTo(TransferStatus.BACEN_PENDING);
        }
        
        @Test
        @DisplayName("Deve marcar como BACEN notificado")
        void shouldMarkAsBacenNotified() {
            Transfer transfer = createPendingTransfer();
            transfer.startProcessing();
            transfer.complete();
            transfer.markBacenPending();
            
            transfer.markBacenNotified("BCN-123456");
            
            assertThat(transfer.getStatus()).isEqualTo(TransferStatus.BACEN_NOTIFIED);
            assertThat(transfer.getBacenNotificationId()).isEqualTo("BCN-123456");
            assertThat(transfer.getBacenNotifiedAt()).isNotNull();
        }
    }
    
    @Nested
    @DisplayName("Retry BACEN")
    class BacenRetryTests {
        
        @Test
        @DisplayName("Deve incrementar contador de retry")
        void shouldIncrementRetryCount() {
            Transfer transfer = createPendingTransfer();
            transfer.startProcessing();
            transfer.complete();
            transfer.markBacenPending();
            
            transfer.incrementBacenRetryCount();
            
            assertThat(transfer.getBacenRetryCount()).isEqualTo(1);
        }
        
        @Test
        @DisplayName("Deve incrementar múltiplas vezes")
        void shouldIncrementMultipleTimes() {
            Transfer transfer = createPendingTransfer();
            transfer.startProcessing();
            transfer.complete();
            transfer.markBacenPending();
            
            transfer.incrementBacenRetryCount();
            transfer.incrementBacenRetryCount();
            transfer.incrementBacenRetryCount();
            
            assertThat(transfer.getBacenRetryCount()).isEqualTo(3);
        }
        
        @Test
        @DisplayName("Deve verificar se pode retry quando BACEN pending")
        void shouldCheckCanRetry() {
            Transfer transfer = createPendingTransfer();
            transfer.startProcessing();
            transfer.complete();
            transfer.markBacenPending();
            
            assertThat(transfer.canRetry()).isTrue();
        }
        
        @Test
        @DisplayName("Transferência BACEN notificada não pode retry")
        void bacenNotifiedTransferCannotRetry() {
            Transfer transfer = createPendingTransfer();
            transfer.startProcessing();
            transfer.complete();
            transfer.markBacenPending();
            transfer.markBacenNotified("BCN-123");
            
            assertThat(transfer.canRetry()).isFalse();
        }
    }
    
    @Nested
    @DisplayName("Validações de Estado")
    class StateValidationTests {
        
        @Test
        @DisplayName("Deve identificar status de BACEN notificada")
        void shouldIdentifyBacenNotifiedStatus() {
            Transfer transfer = createPendingTransfer();
            transfer.startProcessing();
            transfer.complete();
            transfer.markBacenPending();
            transfer.markBacenNotified("BCN-123");
            
            assertThat(transfer.isCompleted()).isTrue();
        }
        
        @Test
        @DisplayName("Deve identificar status de falha")
        void shouldIdentifyFailureStatus() {
            Transfer transfer = createPendingTransfer();
            transfer.startProcessing();
            transfer.fail("Error");
            
            assertThat(transfer.isCompleted()).isFalse();
            assertThat(transfer.getStatus()).isEqualTo(TransferStatus.FAILED);
        }
        
        @Test
        @DisplayName("Pending não é completada")
        void pendingIsNotCompleted() {
            Transfer transfer = createPendingTransfer();
            
            assertThat(transfer.isCompleted()).isFalse();
        }
        
        @Test
        @DisplayName("COMPLETED é completada")
        void completedIsCompleted() {
            Transfer transfer = createPendingTransfer();
            transfer.startProcessing();
            transfer.complete();
            
            assertThat(transfer.isCompleted()).isTrue();
        }
    }
    
    @Nested
    @DisplayName("Transições Inválidas")
    class InvalidTransitionTests {
        
        @Test
        @DisplayName("Não deve completar transferência pendente")
        void shouldNotCompletePendingTransfer() {
            Transfer transfer = createPendingTransfer();
            
            assertThatThrownBy(transfer::complete)
                .isInstanceOf(IllegalStateException.class);
        }
        
        @Test
        @DisplayName("Não deve processar transferência já completada")
        void shouldNotProcessCompletedTransfer() {
            Transfer transfer = createPendingTransfer();
            transfer.startProcessing();
            transfer.complete();
            
            assertThatThrownBy(transfer::startProcessing)
                .isInstanceOf(IllegalStateException.class);
        }
        
        @Test
        @DisplayName("Não deve processar transferência falhada")
        void shouldNotProcessFailedTransfer() {
            Transfer transfer = createPendingTransfer();
            transfer.startProcessing();
            transfer.fail("Error");
            
            assertThatThrownBy(transfer::startProcessing)
                .isInstanceOf(IllegalStateException.class);
        }
    }
    
    @Nested
    @DisplayName("Equals e HashCode")
    class EqualsHashCodeTests {
        
        @Test
        @DisplayName("Transferências com mesmo ID devem ser iguais")
        void transfersWithSameIdShouldBeEqual() {
            TransferId id = TransferId.generate();
            
            Transfer t1 = Transfer.builder()
                .id(id)
                .sourceAccountId(sourceAccountId)
                .targetAccountId(targetAccountId)
                .amount(amount)
                .status(TransferStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
            
            Transfer t2 = Transfer.builder()
                .id(id)
                .sourceAccountId(AccountId.generate())
                .targetAccountId(AccountId.generate())
                .amount(Money.of("500.00"))
                .status(TransferStatus.COMPLETED)
                .createdAt(LocalDateTime.now())
                .build();
            
            assertThat(t1).isEqualTo(t2);
            assertThat(t1.hashCode()).isEqualTo(t2.hashCode());
        }
        
        @Test
        @DisplayName("Transferências com IDs diferentes não devem ser iguais")
        void transfersWithDifferentIdsShouldNotBeEqual() {
            Transfer t1 = createPendingTransfer();
            Transfer t2 = createPendingTransfer();
            
            assertThat(t1).isNotEqualTo(t2);
        }
    }
}
