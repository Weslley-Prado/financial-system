package com.itau.transferapi.application.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO de resposta para consulta de saldo.
 */
@Builder
@Schema(description = "Resposta de consulta de saldo")
public record BalanceResponse(
    
    @Schema(description = "Número da conta")
    String accountNumber,
    
    @Schema(description = "Número da agência")
    String agencyNumber,
    
    @Schema(description = "Nome do titular")
    String holderName,
    
    @Schema(description = "Saldo atual")
    BigDecimal balance,
    
    @Schema(description = "Saldo formatado", example = "R$ 5.000,00")
    String formattedBalance,
    
    @Schema(description = "Limite disponível")
    BigDecimal availableLimit,
    
    @Schema(description = "Limite disponível formatado", example = "R$ 10.000,00")
    String formattedAvailableLimit,
    
    @Schema(description = "Limite diário disponível para transferência")
    BigDecimal dailyTransferLimitAvailable,
    
    @Schema(description = "Limite diário formatado", example = "R$ 800,00")
    String formattedDailyTransferLimit,
    
    @Schema(description = "Data/hora da consulta")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime queryTime
) {}


