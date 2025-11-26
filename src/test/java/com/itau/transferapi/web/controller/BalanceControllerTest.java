package com.itau.transferapi.web.controller;

import com.itau.transferapi.application.dto.response.BalanceResponse;
import com.itau.transferapi.application.port.input.BalanceQueryUseCase;
import com.itau.transferapi.domain.exception.BusinessException;
import com.itau.transferapi.domain.exception.ErrorCode;
import com.itau.transferapi.domain.exception.ResourceNotFoundException;
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

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BalanceController.class)
@DisplayName("BalanceController Tests")
class BalanceControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private BalanceQueryUseCase balanceQueryUseCase;
    
    @Nested
    @DisplayName("GET /api/v1/accounts/{accountNumber}/balance")
    class GetBalanceTests {
        
        @Test
        @DisplayName("Deve retornar saldo com sucesso")
        void shouldReturnBalanceSuccessfully() throws Exception {
            BalanceResponse response = BalanceResponse.builder()
                .accountNumber("12345-6")
                .agencyNumber("0001")
                .holderName("João Silva")
                .balance(new BigDecimal("5000.00"))
                .formattedBalance("R$ 5.000,00")
                .availableLimit(new BigDecimal("10000.00"))
                .formattedAvailableLimit("R$ 10.000,00")
                .dailyTransferLimitAvailable(new BigDecimal("1000.00"))
                .formattedDailyTransferLimit("R$ 1.000,00")
                .queryTime(LocalDateTime.now())
                .build();
            
            when(balanceQueryUseCase.getBalance(anyString(), anyString())).thenReturn(response);
            
            mockMvc.perform(get("/api/v1/accounts/12345-6/balance")
                    .param("agencyNumber", "0001")
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("12345-6"))
                .andExpect(jsonPath("$.agencyNumber").value("0001"))
                .andExpect(jsonPath("$.holderName").value("João Silva"))
                .andExpect(jsonPath("$.balance").value(5000.00))
                .andExpect(jsonPath("$.formattedBalance").exists())
                .andExpect(jsonPath("$.availableLimit").value(10000.00))
                .andExpect(jsonPath("$.dailyTransferLimitAvailable").value(1000.00));
        }
        
        @Test
        @DisplayName("Deve retornar 404 para conta não encontrada")
        void shouldReturn404ForAccountNotFound() throws Exception {
            when(balanceQueryUseCase.getBalance(anyString(), anyString()))
                .thenThrow(ResourceNotFoundException.account("99999-9"));
            
            mockMvc.perform(get("/api/v1/accounts/99999-9/balance")
                    .param("agencyNumber", "9999")
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ITAU-3001"));
        }
        
        @Test
        @DisplayName("Deve retornar 422 para conta inativa")
        void shouldReturn422ForInactiveAccount() throws Exception {
            when(balanceQueryUseCase.getBalance(anyString(), anyString()))
                .thenThrow(new BusinessException(ErrorCode.ACCOUNT_NOT_ACTIVE, "Conta 11111-1 não está ativa"));
            
            mockMvc.perform(get("/api/v1/accounts/11111-1/balance")
                    .param("agencyNumber", "0001")
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("ITAU-2001"));
        }
        
        @Test
        @DisplayName("Deve retornar erro sem parâmetro de agência")
        void shouldReturnErrorWithoutAgencyParameter() throws Exception {
            mockMvc.perform(get("/api/v1/accounts/12345-6/balance")
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError()); // Missing required param returns 500
        }
    }
}
