package com.itau.transferapi.integration;

import com.itau.transferapi.application.dto.request.TransferRequest;
import com.itau.transferapi.application.dto.response.TransferResponse;
import com.itau.transferapi.domain.valueobject.TransferStatus;
import org.junit.jupiter.api.DisplayName;
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
    
    @Test
    @DisplayName("Deve realizar transferência com sucesso via API")
    void shouldExecuteTransferViaApi() {
        TransferRequest request = TransferRequest.builder()
            .sourceAccountNumber("12345-6")
            .sourceAgencyNumber("0001")
            .targetAccountNumber("98765-4")
            .targetAgencyNumber("0002")
            .amount(new BigDecimal("100.00"))
            .build();
        
        ResponseEntity<TransferResponse> response = restTemplate.postForEntity(
            getBaseUrl() + "/transfers",
            request,
            TransferResponse.class
        );
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().transferId()).isNotNull();
        assertThat(response.getBody().amount())
            .isEqualByComparingTo(new BigDecimal("100.00"));
    }
    
    @Test
    @DisplayName("Deve retornar erro 400 para requisição inválida")
    void shouldReturn400ForInvalidRequest() {
        TransferRequest invalidRequest = TransferRequest.builder()
            .sourceAccountNumber("")  // Inválido: vazio
            .sourceAgencyNumber("0001")
            .targetAccountNumber("98765-4")
            .targetAgencyNumber("0002")
            .amount(new BigDecimal("100.00"))
            .build();
        
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
        TransferRequest request = TransferRequest.builder()
            .sourceAccountNumber("99999-9")
            .sourceAgencyNumber("9999")
            .targetAccountNumber("98765-4")
            .targetAgencyNumber("0002")
            .amount(new BigDecimal("100.00"))
            .build();
        
        ResponseEntity<String> response = restTemplate.postForEntity(
            getBaseUrl() + "/transfers",
            request,
            String.class
        );
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
    
    @Test
    @DisplayName("Deve consultar saldo com sucesso")
    void shouldGetBalanceSuccessfully() {
        ResponseEntity<String> response = restTemplate.getForEntity(
            getBaseUrl() + "/accounts/12345-6/balance?agencyNumber=0001",
            String.class
        );
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).contains("12345-6");
    }
}


