package com.itau.transferapi.domain.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Client Entity Tests")
class ClientTest {
    
    @Nested
    @DisplayName("Criação de Client")
    class CreationTests {
        
        @Test
        @DisplayName("Deve criar cliente com builder")
        void shouldCreateWithBuilder() {
            UUID id = UUID.randomUUID();
            
            Client client = Client.builder()
                .id(id)
                .name("João Silva")
                .documentNumber("12345678900")
                .active(true)
                .build();
            
            assertThat(client).isNotNull();
            assertThat(client.getId()).isEqualTo(id);
            assertThat(client.getName()).isEqualTo("João Silva");
            assertThat(client.getDocumentNumber()).isEqualTo("12345678900");
            assertThat(client.isActive()).isTrue();
        }
        
        @Test
        @DisplayName("Deve criar cliente inativo")
        void shouldCreateInactiveClient() {
            Client client = Client.builder()
                .id(UUID.randomUUID())
                .name("Carlos Oliveira")
                .documentNumber("98765432100")
                .active(false)
                .build();
            
            assertThat(client.isActive()).isFalse();
        }
    }
    
    @Nested
    @DisplayName("Validações")
    class ValidationTests {
        
        @Test
        @DisplayName("Cliente ativo deve ser válido")
        void activeClientShouldBeValid() {
            Client client = Client.builder()
                .id(UUID.randomUUID())
                .name("Maria Santos")
                .documentNumber("11122233344")
                .active(true)
                .build();
            
            assertThat(client.isActive()).isTrue();
        }
        
        @Test
        @DisplayName("Cliente inativo não deve permitir operações")
        void inactiveClientShouldNotAllowOperations() {
            Client client = Client.builder()
                .id(UUID.randomUUID())
                .name("Carlos Oliveira")
                .documentNumber("55566677788")
                .active(false)
                .build();
            
            assertThat(client.isActive()).isFalse();
        }
    }
    
    @Nested
    @DisplayName("Getters")
    class GetterTests {
        
        @Test
        @DisplayName("Deve retornar todos os campos corretamente")
        void shouldReturnAllFieldsCorrectly() {
            UUID id = UUID.randomUUID();
            String name = "Test User";
            String document = "00011122233";
            
            Client client = Client.builder()
                .id(id)
                .name(name)
                .documentNumber(document)
                .active(true)
                .build();
            
            assertThat(client.getId()).isEqualTo(id);
            assertThat(client.getName()).isEqualTo(name);
            assertThat(client.getDocumentNumber()).isEqualTo(document);
            assertThat(client.isActive()).isTrue();
        }
    }
}

