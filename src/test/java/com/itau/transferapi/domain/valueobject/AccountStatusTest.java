package com.itau.transferapi.domain.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("AccountStatus Enum Tests")
class AccountStatusTest {
    
    @Test
    @DisplayName("Deve ter 4 status possíveis")
    void shouldHaveFourStatuses() {
        assertThat(AccountStatus.values()).hasSize(4);
    }
    
    @Test
    @DisplayName("ACTIVE deve permitir operações")
    void activeShouldAllowOperations() {
        assertThat(AccountStatus.ACTIVE.allowsOperations()).isTrue();
    }
    
    @Test
    @DisplayName("INACTIVE não deve permitir operações")
    void inactiveShouldNotAllowOperations() {
        assertThat(AccountStatus.INACTIVE.allowsOperations()).isFalse();
    }
    
    @Test
    @DisplayName("BLOCKED não deve permitir operações")
    void blockedShouldNotAllowOperations() {
        assertThat(AccountStatus.BLOCKED.allowsOperations()).isFalse();
    }
    
    @Test
    @DisplayName("CLOSED não deve permitir operações")
    void closedShouldNotAllowOperations() {
        assertThat(AccountStatus.CLOSED.allowsOperations()).isFalse();
    }
    
    @ParameterizedTest
    @EnumSource(AccountStatus.class)
    @DisplayName("Todos os status devem ter descrição")
    void allStatusesShouldHaveDescription(AccountStatus status) {
        assertThat(status.getDescription()).isNotNull();
        assertThat(status.getDescription()).isNotBlank();
    }
    
    @Test
    @DisplayName("Descrições devem estar em português")
    void descriptionsShouldBeInPortuguese() {
        assertThat(AccountStatus.ACTIVE.getDescription()).isEqualTo("Ativa");
        assertThat(AccountStatus.INACTIVE.getDescription()).isEqualTo("Inativa");
        assertThat(AccountStatus.BLOCKED.getDescription()).isEqualTo("Bloqueada");
        assertThat(AccountStatus.CLOSED.getDescription()).isEqualTo("Encerrada");
    }
    
    @Test
    @DisplayName("Deve converter de String")
    void shouldConvertFromString() {
        assertThat(AccountStatus.valueOf("ACTIVE")).isEqualTo(AccountStatus.ACTIVE);
        assertThat(AccountStatus.valueOf("INACTIVE")).isEqualTo(AccountStatus.INACTIVE);
        assertThat(AccountStatus.valueOf("BLOCKED")).isEqualTo(AccountStatus.BLOCKED);
        assertThat(AccountStatus.valueOf("CLOSED")).isEqualTo(AccountStatus.CLOSED);
    }
}

