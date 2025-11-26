package com.itau.transferapi.integration;

import com.itau.transferapi.application.dto.request.TransferRequest;
import com.itau.transferapi.application.dto.response.BalanceResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("Transfer API Integration Tests")
class TransferApiIntegrationTest {
    
    @LocalServerPort
    private int port;
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    private String getBaseUrl() {
        return "http://localhost:" + port + "/api/v1";
    }
    
    @Nested
    @DisplayName("POST /api/v1/transfers")
    class TransferTests {
        
        @Test
        @DisplayName("Deve retornar erro 400 para requisição inválida")
        void shouldReturn400ForInvalidRequest() {
            TransferRequest invalidRequest = new TransferRequest(
                "", "0001",
                "98765-4", "0002",
                new BigDecimal("100.00"),
                null
            );
            
            ResponseEntity<String> response = restTemplate.postForEntity(
                getBaseUrl() + "/transfers",
                invalidRequest,
                String.class
            );
            
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
        
        @Test
        @DisplayName("Deve retornar erro 404 para conta inexistente")
        void shouldReturn404ForNonExistentAccount() {
            TransferRequest request = new TransferRequest(
                "99999-9", "9999",
                "98765-4", "0002",
                new BigDecimal("100.00"),
                null
            );
            
            ResponseEntity<String> response = restTemplate.postForEntity(
                getBaseUrl() + "/transfers",
                request,
                String.class
            );
            
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }
    
    @Nested
    @DisplayName("GET /api/v1/accounts/{account}/balance")
    class BalanceTests {
        
        @Test
        @DisplayName("Deve consultar saldo com sucesso")
        void shouldGetBalanceSuccessfully() {
            ResponseEntity<BalanceResponse> response = restTemplate.getForEntity(
                getBaseUrl() + "/accounts/12345-6/balance?agencyNumber=0001",
                BalanceResponse.class
            );
            
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().accountNumber()).isEqualTo("12345-6");
            assertThat(response.getBody().balance()).isNotNull();
        }
        
        @Test
        @DisplayName("Deve retornar 404 para conta não encontrada")
        void shouldReturn404ForNonExistentAccount() {
            ResponseEntity<String> response = restTemplate.getForEntity(
                getBaseUrl() + "/accounts/99999-9/balance?agencyNumber=9999",
                String.class
            );
            
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }
}
