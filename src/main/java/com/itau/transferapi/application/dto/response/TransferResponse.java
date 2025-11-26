package com.itau.transferapi.application.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.itau.transferapi.domain.valueobject.TransferStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de resposta para transferência.
 */
@Builder
@Schema(description = "Resposta de transferência bancária")
public record TransferResponse(
    
    @Schema(description = "ID único da transferência")
    UUID transferId,
    
    @Schema(description = "Status da transferência")
    TransferStatus status,
    
    @Schema(description = "Valor da transferência")
    BigDecimal amount,
    
    @Schema(description = "Valor formatado da transferência", example = "R$ 150,00")
    String formattedAmount,
    
    @Schema(description = "Número da conta de origem")
    String sourceAccountNumber,
    
    @Schema(description = "Número da agência de origem")
    String sourceAgencyNumber,
    
    @Schema(description = "Número da conta de destino")
    String targetAccountNumber,
    
    @Schema(description = "Número da agência de destino")
    String targetAgencyNumber,
    
    @Schema(description = "Data/hora de criação")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime createdAt,
    
    @Schema(description = "Data/hora de conclusão")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime completedAt,
    
    @Schema(description = "ID de notificação do BACEN")
    String bacenNotificationId,
    
    @Schema(description = "Mensagem adicional")
    String message
) {}


