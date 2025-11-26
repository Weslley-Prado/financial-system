package com.itau.transferapi.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itau.transferapi.application.dto.request.TransferRequest;
import com.itau.transferapi.application.dto.response.TransferResponse;
import com.itau.transferapi.application.port.input.TransferUseCase;
import com.itau.transferapi.domain.exception.BusinessException;
import com.itau.transferapi.domain.exception.ErrorCode;
import com.itau.transferapi.domain.exception.ResourceNotFoundException;
import com.itau.transferapi.domain.valueobject.TransferStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransferController.class)
@DisplayName("TransferController Tests")
class TransferControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private TransferUseCase transferUseCase;
    
    @Nested
    @DisplayName("POST /api/v1/transfers")
    class CreateTransferTests {
        
        @Test
        @DisplayName("Deve criar transferência com sucesso")
        void shouldCreateTransferSuccessfully() throws Exception {
            TransferRequest request = new TransferRequest(
                "12345-6", "0001", "98765-4", "0002",
                new BigDecimal("100.00"), "Test"
            );
            
            TransferResponse response = TransferResponse.builder()
                .transferId(UUID.randomUUID())
                .status(TransferStatus.BACEN_NOTIFIED)
                .amount(new BigDecimal("100.00"))
                .formattedAmount("R$ 100,00")
                .sourceAccountNumber("12345-6")
                .sourceAgencyNumber("0001")
                .targetAccountNumber("98765-4")
                .targetAgencyNumber("0002")
                .createdAt(LocalDateTime.now())
                .completedAt(LocalDateTime.now())
                .bacenNotificationId("BCN-123")
                .message("Transferência realizada com sucesso")
                .build();
            
            when(transferUseCase.execute(any())).thenReturn(response);
            
            mockMvc.perform(post("/api/v1/transfers")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("BACEN_NOTIFIED"))
                .andExpect(jsonPath("$.amount").value(100.00));
        }
        
        @Test
        @DisplayName("Deve retornar 422 para saldo insuficiente")
        void shouldReturn422ForInsufficientBalance() throws Exception {
            TransferRequest request = new TransferRequest(
                "12345-6", "0001", "98765-4", "0002",
                new BigDecimal("100.00"), null
            );
            
            when(transferUseCase.execute(any()))
                .thenThrow(new BusinessException(ErrorCode.INSUFFICIENT_BALANCE, "Saldo de R$ 50 é insuficiente para R$ 100"));
            
            mockMvc.perform(post("/api/v1/transfers")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("ITAU-2002"));
        }
        
        @Test
        @DisplayName("Deve retornar 400 para mesma conta")
        void shouldReturn400ForSameAccount() throws Exception {
            TransferRequest request = new TransferRequest(
                "12345-6", "0001", "12345-6", "0001",
                new BigDecimal("100.00"), null
            );
            
            when(transferUseCase.execute(any()))
                .thenThrow(new BusinessException(ErrorCode.SAME_ACCOUNT_TRANSFER));
            
            mockMvc.perform(post("/api/v1/transfers")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ITAU-1004"));
        }
        
        @Test
        @DisplayName("Deve retornar 404 para conta não encontrada")
        void shouldReturn404ForAccountNotFound() throws Exception {
            TransferRequest request = new TransferRequest(
                "99999-9", "9999", "98765-4", "0002",
                new BigDecimal("100.00"), null
            );
            
            when(transferUseCase.execute(any()))
                .thenThrow(ResourceNotFoundException.account("99999-9"));
            
            mockMvc.perform(post("/api/v1/transfers")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ITAU-3001"));
        }
        
        @Test
        @DisplayName("Deve retornar 400 para requisição inválida")
        void shouldReturn400ForInvalidRequest() throws Exception {
            String invalidJson = "{\"sourceAccountNumber\":\"\",\"amount\":-100}";
            
            mockMvc.perform(post("/api/v1/transfers")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson))
                .andExpect(status().isBadRequest());
        }
        
        @Test
        @DisplayName("Deve retornar 422 para limite diário excedido")
        void shouldReturn422ForDailyLimitExceeded() throws Exception {
            TransferRequest request = new TransferRequest(
                "12345-6", "0001", "98765-4", "0002",
                new BigDecimal("1500.00"), null
            );
            
            when(transferUseCase.execute(any()))
                .thenThrow(new BusinessException(ErrorCode.DAILY_LIMIT_EXCEEDED, "Limite diário excedido"));
            
            mockMvc.perform(post("/api/v1/transfers")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("ITAU-2004"));
        }
        
        @Test
        @DisplayName("Deve retornar 422 para conta inativa")
        void shouldReturn422ForInactiveAccount() throws Exception {
            TransferRequest request = new TransferRequest(
                "11111-1", "0001", "98765-4", "0002",
                new BigDecimal("100.00"), null
            );
            
            when(transferUseCase.execute(any()))
                .thenThrow(new BusinessException(ErrorCode.ACCOUNT_NOT_ACTIVE, "Conta 11111-1 não está ativa"));
            
            mockMvc.perform(post("/api/v1/transfers")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("ITAU-2001"));
        }
    }
}
