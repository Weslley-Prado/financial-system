package com.itau.transferapi.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Builder;

import java.math.BigDecimal;

/**
 * DTO de requisição para criação de transferência.
 */
@Builder
@Schema(description = "Requisição de transferência bancária")
public record TransferRequest(
    
    @NotBlank(message = "Número da conta de origem é obrigatório")
    @Size(min = 5, max = 10, message = "Número da conta deve ter entre 5 e 10 caracteres")
    @Schema(description = "Número da conta de origem", example = "12345-6")
    String sourceAccountNumber,
    
    @NotBlank(message = "Número da agência de origem é obrigatório")
    @Size(min = 4, max = 6, message = "Número da agência deve ter entre 4 e 6 caracteres")
    @Schema(description = "Número da agência de origem", example = "0001")
    String sourceAgencyNumber,
    
    @NotBlank(message = "Número da conta de destino é obrigatório")
    @Size(min = 5, max = 10, message = "Número da conta deve ter entre 5 e 10 caracteres")
    @Schema(description = "Número da conta de destino", example = "98765-4")
    String targetAccountNumber,
    
    @NotBlank(message = "Número da agência de destino é obrigatório")
    @Size(min = 4, max = 6, message = "Número da agência deve ter entre 4 e 6 caracteres")
    @Schema(description = "Número da agência de destino", example = "0002")
    String targetAgencyNumber,
    
    @NotNull(message = "Valor da transferência é obrigatório")
    @DecimalMin(value = "0.01", message = "Valor mínimo para transferência é R$ 0,01")
    @DecimalMax(value = "100000.00", message = "Valor máximo para transferência é R$ 100.000,00")
    @Digits(integer = 8, fraction = 2, message = "Valor deve ter no máximo 8 dígitos inteiros e 2 decimais")
    @Schema(description = "Valor da transferência", example = "150.00")
    BigDecimal amount,
    
    @Size(max = 140, message = "Descrição deve ter no máximo 140 caracteres")
    @Schema(description = "Descrição opcional da transferência", example = "Pagamento referente ao mês de janeiro")
    String description
) {
    
    /**
     * Valida se origem e destino são diferentes.
     * 
     * @return true se são diferentes
     */
    public boolean isDifferentAccounts() {
        return !(sourceAccountNumber.equals(targetAccountNumber) && 
                 sourceAgencyNumber.equals(targetAgencyNumber));
    }
}


