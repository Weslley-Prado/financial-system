package com.itau.transferapi.web.controller;

import com.itau.transferapi.application.dto.response.BalanceResponse;
import com.itau.transferapi.application.dto.response.ErrorResponse;
import com.itau.transferapi.application.port.input.BalanceQueryUseCase;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Controller REST para operações de Consulta de Saldo.
 * 
 * Endpoints:
 * - GET /api/v1/accounts/{accountNumber}/balance - Consultar saldo
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
@Validated
@Tag(name = "Saldo", description = "Operações de consulta de saldo")
public class BalanceController {
    
    private final BalanceQueryUseCase balanceQueryUseCase;
    
    @GetMapping(
        value = "/{accountNumber}/balance",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
        summary = "Consultar saldo",
        description = "Consulta o saldo e limites de uma conta corrente"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Saldo consultado com sucesso",
            content = @Content(schema = @Schema(implementation = BalanceResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Conta não encontrada",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "422",
            description = "Conta inativa",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    @Timed(
        value = "balance.query.time",
        description = "Tempo de consulta de saldo",
        percentiles = {0.5, 0.95, 0.99}
    )
    public ResponseEntity<BalanceResponse> getBalance(
            @Parameter(description = "Número da conta", example = "12345-6")
            @PathVariable 
            @NotBlank 
            @Size(min = 5, max = 10) 
            String accountNumber,
            
            @Parameter(description = "Número da agência", example = "0001")
            @RequestParam 
            @NotBlank 
            @Size(min = 4, max = 6) 
            String agencyNumber) {
        
        log.debug("Consultando saldo: conta={}, agência={}", accountNumber, agencyNumber);
        
        BalanceResponse response = balanceQueryUseCase.getBalance(accountNumber, agencyNumber);
        
        return ResponseEntity.ok(response);
    }
}


