package com.itau.transferapi.web.controller;

import com.itau.transferapi.application.dto.request.TransferRequest;
import com.itau.transferapi.application.dto.response.ErrorResponse;
import com.itau.transferapi.application.dto.response.TransferResponse;
import com.itau.transferapi.application.port.input.TransferUseCase;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller REST para operações de Transferência.
 * 
 * Endpoints:
 * - POST /api/v1/transfers - Criar transferência
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/transfers")
@RequiredArgsConstructor
@Tag(name = "Transferências", description = "Operações de transferência bancária")
public class TransferController {
    
    private final TransferUseCase transferUseCase;
    
    @PostMapping(
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        summary = "Realizar transferência",
        description = "Realiza uma transferência bancária entre contas correntes"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Transferência realizada com sucesso",
            content = @Content(schema = @Schema(implementation = TransferResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Dados inválidos na requisição",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Conta não encontrada",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "422",
            description = "Erro de regra de negócio (saldo insuficiente, limite excedido, etc)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "503",
            description = "Serviço temporariamente indisponível",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    @Timed(
        value = "transfer.execution.time",
        description = "Tempo de execução de transferência",
        percentiles = {0.5, 0.95, 0.99}
    )
    public ResponseEntity<TransferResponse> createTransfer(
            @Valid @RequestBody TransferRequest request) {
        
        log.info("Recebida requisição de transferência: origem={}/{} -> destino={}/{}, valor={}",
            request.sourceAccountNumber(), request.sourceAgencyNumber(),
            request.targetAccountNumber(), request.targetAgencyNumber(),
            request.amount());
        
        TransferResponse response = transferUseCase.execute(request);
        
        log.info("Transferência processada: id={}, status={}", 
            response.transferId(), response.status());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}


