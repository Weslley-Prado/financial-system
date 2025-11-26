package com.itau.transferapi.domain.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("AccountId Value Object Tests")
class AccountIdTest {
    
    @Nested
    @DisplayName("Criação")
    class CreationTests {
        
        @Test
        @DisplayName("Deve criar AccountId a partir de UUID")
        void shouldCreateFromUUID() {
            UUID uuid = UUID.randomUUID();
            AccountId accountId = AccountId.of(uuid);
            
            assertThat(accountId.value()).isEqualTo(uuid);
        }
        
        @Test
        @DisplayName("Deve criar AccountId a partir de String")
        void shouldCreateFromString() {
            String uuidStr = "550e8400-e29b-41d4-a716-446655440000";
            AccountId accountId = AccountId.of(uuidStr);
            
            assertThat(accountId.value()).isEqualTo(UUID.fromString(uuidStr));
        }
        
        @Test
        @DisplayName("Deve gerar novo AccountId")
        void shouldGenerateNew() {
            AccountId accountId = AccountId.generate();
            
            assertThat(accountId).isNotNull();
            assertThat(accountId.value()).isNotNull();
        }
        
        @Test
        @DisplayName("Deve lançar exceção para UUID nulo")
        void shouldThrowExceptionForNullUUID() {
            assertThatThrownBy(() -> AccountId.of((UUID) null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("não pode ser nulo");
        }
        
        @Test
        @DisplayName("Deve lançar exceção para String nula")
        void shouldThrowExceptionForNullString() {
            assertThatThrownBy(() -> AccountId.of((String) null))
                .isInstanceOf(NullPointerException.class);
        }
        
        @Test
        @DisplayName("Deve lançar exceção para String vazia")
        void shouldThrowExceptionForEmptyString() {
            assertThatThrownBy(() -> AccountId.of(""))
                .isInstanceOf(IllegalArgumentException.class);
        }
        
        @Test
        @DisplayName("Deve lançar exceção para String inválida")
        void shouldThrowExceptionForInvalidString() {
            assertThatThrownBy(() -> AccountId.of("invalid-uuid"))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }
    
    @Nested
    @DisplayName("Equals e HashCode")
    class EqualsHashCodeTests {
        
        @Test
        @DisplayName("AccountIds iguais devem ser equals")
        void equalAccountIdsShouldBeEqual() {
            UUID uuid = UUID.randomUUID();
            AccountId a = AccountId.of(uuid);
            AccountId b = AccountId.of(uuid);
            
            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }
        
        @Test
        @DisplayName("AccountIds diferentes não devem ser equals")
        void differentAccountIdsShouldNotBeEqual() {
            AccountId a = AccountId.generate();
            AccountId b = AccountId.generate();
            
            assertThat(a).isNotEqualTo(b);
        }
        
        @Test
        @DisplayName("Deve lidar com comparação com null")
        void shouldHandleNullComparison() {
            AccountId accountId = AccountId.generate();
            assertThat(accountId).isNotEqualTo(null);
        }
        
        @Test
        @DisplayName("Deve lidar com comparação com outro tipo")
        void shouldHandleDifferentTypeComparison() {
            AccountId accountId = AccountId.generate();
            assertThat(accountId).isNotEqualTo("string");
        }
    }
    
    @Nested
    @DisplayName("ToString")
    class ToStringTests {
        
        @Test
        @DisplayName("ToString deve retornar representação UUID")
        void toStringShouldReturnUUIDRepresentation() {
            UUID uuid = UUID.randomUUID();
            AccountId accountId = AccountId.of(uuid);
            
            assertThat(accountId.toString()).contains(uuid.toString());
        }
    }
}

