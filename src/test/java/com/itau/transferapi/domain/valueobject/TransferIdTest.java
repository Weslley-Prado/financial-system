package com.itau.transferapi.domain.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("TransferId Value Object Tests")
class TransferIdTest {
    
    @Nested
    @DisplayName("Criação")
    class CreationTests {
        
        @Test
        @DisplayName("Deve criar TransferId a partir de UUID")
        void shouldCreateFromUUID() {
            UUID uuid = UUID.randomUUID();
            TransferId transferId = TransferId.of(uuid);
            
            assertThat(transferId.value()).isEqualTo(uuid);
        }
        
        @Test
        @DisplayName("Deve criar TransferId a partir de String")
        void shouldCreateFromString() {
            String uuidStr = "550e8400-e29b-41d4-a716-446655440000";
            TransferId transferId = TransferId.of(uuidStr);
            
            assertThat(transferId.value()).isEqualTo(UUID.fromString(uuidStr));
        }
        
        @Test
        @DisplayName("Deve gerar novo TransferId")
        void shouldGenerateNew() {
            TransferId transferId = TransferId.generate();
            
            assertThat(transferId).isNotNull();
            assertThat(transferId.value()).isNotNull();
        }
        
        @Test
        @DisplayName("Deve lançar exceção para UUID nulo")
        void shouldThrowExceptionForNullUUID() {
            assertThatThrownBy(() -> TransferId.of((UUID) null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("não pode ser nulo");
        }
        
        @Test
        @DisplayName("Deve lançar exceção para String nula")
        void shouldThrowExceptionForNullString() {
            assertThatThrownBy(() -> TransferId.of((String) null))
                .isInstanceOf(NullPointerException.class);
        }
        
        @Test
        @DisplayName("Deve lançar exceção para String vazia")
        void shouldThrowExceptionForEmptyString() {
            assertThatThrownBy(() -> TransferId.of(""))
                .isInstanceOf(IllegalArgumentException.class);
        }
        
        @Test
        @DisplayName("Deve lançar exceção para String inválida")
        void shouldThrowExceptionForInvalidString() {
            assertThatThrownBy(() -> TransferId.of("not-a-uuid"))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }
    
    @Nested
    @DisplayName("Equals e HashCode")
    class EqualsHashCodeTests {
        
        @Test
        @DisplayName("TransferIds iguais devem ser equals")
        void equalTransferIdsShouldBeEqual() {
            UUID uuid = UUID.randomUUID();
            TransferId a = TransferId.of(uuid);
            TransferId b = TransferId.of(uuid);
            
            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }
        
        @Test
        @DisplayName("TransferIds diferentes não devem ser equals")
        void differentTransferIdsShouldNotBeEqual() {
            TransferId a = TransferId.generate();
            TransferId b = TransferId.generate();
            
            assertThat(a).isNotEqualTo(b);
        }
    }
    
    @Nested
    @DisplayName("ToString")
    class ToStringTests {
        
        @Test
        @DisplayName("ToString deve retornar representação UUID")
        void toStringShouldReturnUUIDRepresentation() {
            UUID uuid = UUID.randomUUID();
            TransferId transferId = TransferId.of(uuid);
            
            assertThat(transferId.toString()).contains(uuid.toString());
        }
    }
}

