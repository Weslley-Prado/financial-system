package com.itau.transferapi.domain.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("TransferStatus Enum Tests")
class TransferStatusTest {
    
    @Test
    @DisplayName("Deve ter 6 status possíveis")
    void shouldHaveSixStatuses() {
        assertThat(TransferStatus.values()).hasSize(6);
    }
    
    @Nested
    @DisplayName("Status Finais")
    class FinalStatusTests {
        
        @Test
        @DisplayName("BACEN_NOTIFIED deve ser status final")
        void bacenNotifiedShouldBeFinal() {
            assertThat(TransferStatus.BACEN_NOTIFIED.isFinal()).isTrue();
        }
        
        @Test
        @DisplayName("FAILED deve ser status final")
        void failedShouldBeFinal() {
            assertThat(TransferStatus.FAILED.isFinal()).isTrue();
        }
        
        @Test
        @DisplayName("Status não finais")
        void nonFinalStatuses() {
            assertThat(TransferStatus.PENDING.isFinal()).isFalse();
            assertThat(TransferStatus.PROCESSING.isFinal()).isFalse();
            assertThat(TransferStatus.COMPLETED.isFinal()).isFalse();
            assertThat(TransferStatus.BACEN_PENDING.isFinal()).isFalse();
        }
    }
    
    @Nested
    @DisplayName("Status de Sucesso")
    class SuccessStatusTests {
        
        @Test
        @DisplayName("COMPLETED deve ser status de sucesso")
        void completedShouldBeSuccess() {
            assertThat(TransferStatus.COMPLETED.isSuccess()).isTrue();
        }
        
        @Test
        @DisplayName("BACEN_NOTIFIED deve ser status de sucesso")
        void bacenNotifiedShouldBeSuccess() {
            assertThat(TransferStatus.BACEN_NOTIFIED.isSuccess()).isTrue();
        }
        
        @Test
        @DisplayName("Status não sucesso")
        void nonSuccessStatuses() {
            assertThat(TransferStatus.PENDING.isSuccess()).isFalse();
            assertThat(TransferStatus.PROCESSING.isSuccess()).isFalse();
            assertThat(TransferStatus.BACEN_PENDING.isSuccess()).isFalse();
            assertThat(TransferStatus.FAILED.isSuccess()).isFalse();
        }
    }
    
    @Nested
    @DisplayName("Transições de Status")
    class StatusTransitionTests {
        
        @Test
        @DisplayName("PENDING pode transicionar para PROCESSING")
        void pendingCanTransitionToProcessing() {
            assertThat(TransferStatus.PENDING.canTransitionTo(TransferStatus.PROCESSING)).isTrue();
        }
        
        @Test
        @DisplayName("PENDING pode transicionar para FAILED")
        void pendingCanTransitionToFailed() {
            assertThat(TransferStatus.PENDING.canTransitionTo(TransferStatus.FAILED)).isTrue();
        }
        
        @Test
        @DisplayName("PROCESSING pode transicionar para COMPLETED")
        void processingCanTransitionToCompleted() {
            assertThat(TransferStatus.PROCESSING.canTransitionTo(TransferStatus.COMPLETED)).isTrue();
        }
        
        @Test
        @DisplayName("PROCESSING pode transicionar para FAILED")
        void processingCanTransitionToFailed() {
            assertThat(TransferStatus.PROCESSING.canTransitionTo(TransferStatus.FAILED)).isTrue();
        }
        
        @Test
        @DisplayName("COMPLETED pode transicionar para BACEN_PENDING")
        void completedCanTransitionToBacenPending() {
            assertThat(TransferStatus.COMPLETED.canTransitionTo(TransferStatus.BACEN_PENDING)).isTrue();
        }
        
        @Test
        @DisplayName("BACEN_PENDING pode transicionar para BACEN_NOTIFIED")
        void bacenPendingCanTransitionToBacenNotified() {
            assertThat(TransferStatus.BACEN_PENDING.canTransitionTo(TransferStatus.BACEN_NOTIFIED)).isTrue();
        }
        
        @Test
        @DisplayName("BACEN_PENDING pode transicionar para FAILED")
        void bacenPendingCanTransitionToFailed() {
            assertThat(TransferStatus.BACEN_PENDING.canTransitionTo(TransferStatus.FAILED)).isTrue();
        }
        
        @Test
        @DisplayName("Status finais não podem transicionar")
        void finalStatusesCannotTransition() {
            assertThat(TransferStatus.BACEN_NOTIFIED.canTransitionTo(TransferStatus.FAILED)).isFalse();
            assertThat(TransferStatus.FAILED.canTransitionTo(TransferStatus.COMPLETED)).isFalse();
        }
        
        @Test
        @DisplayName("Transições inválidas")
        void invalidTransitions() {
            assertThat(TransferStatus.PENDING.canTransitionTo(TransferStatus.COMPLETED)).isFalse();
            assertThat(TransferStatus.PENDING.canTransitionTo(TransferStatus.BACEN_NOTIFIED)).isFalse();
            assertThat(TransferStatus.PROCESSING.canTransitionTo(TransferStatus.BACEN_PENDING)).isFalse();
        }
    }
    
    @ParameterizedTest
    @EnumSource(TransferStatus.class)
    @DisplayName("Todos os status devem ter descrição")
    void allStatusesShouldHaveDescription(TransferStatus status) {
        assertThat(status.getDescription()).isNotNull();
        assertThat(status.getDescription()).isNotBlank();
    }
    
    @Test
    @DisplayName("Descrições devem estar preenchidas")
    void descriptionsShouldBeFilled() {
        assertThat(TransferStatus.PENDING.getDescription()).isNotEmpty();
        assertThat(TransferStatus.PROCESSING.getDescription()).isNotEmpty();
        assertThat(TransferStatus.COMPLETED.getDescription()).isNotEmpty();
        assertThat(TransferStatus.FAILED.getDescription()).isNotEmpty();
        assertThat(TransferStatus.BACEN_PENDING.getDescription()).isNotEmpty();
        assertThat(TransferStatus.BACEN_NOTIFIED.getDescription()).isNotEmpty();
    }
}
