package com.itau.transferapi.application.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO padronizado para respostas de erro.
 */
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Resposta de erro padronizada")
public record ErrorResponse(
    
    @Schema(description = "Código de erro", example = "ITAU-2004")
    String code,
    
    @Schema(description = "Mensagem de erro")
    String message,
    
    @Schema(description = "Detalhes adicionais do erro")
    String details,
    
    @Schema(description = "Caminho da requisição")
    String path,
    
    @Schema(description = "Lista de erros de validação")
    List<FieldError> fieldErrors,
    
    @Schema(description = "Timestamp do erro")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime timestamp,
    
    @Schema(description = "ID de correlação para rastreamento")
    String traceId
) {
    
    /**
     * Representa um erro de validação de campo específico.
     */
    @Builder
    public record FieldError(
        @Schema(description = "Nome do campo")
        String field,
        
        @Schema(description = "Mensagem de erro")
        String message,
        
        @Schema(description = "Valor rejeitado")
        Object rejectedValue
    ) {}
    
    /**
     * Cria uma resposta de erro simples.
     */
    public static ErrorResponse of(String code, String message, String path, String traceId) {
        return ErrorResponse.builder()
            .code(code)
            .message(message)
            .path(path)
            .timestamp(LocalDateTime.now())
            .traceId(traceId)
            .build();
    }
    
    /**
     * Cria uma resposta de erro com detalhes.
     */
    public static ErrorResponse of(String code, String message, String details, String path, String traceId) {
        return ErrorResponse.builder()
            .code(code)
            .message(message)
            .details(details)
            .path(path)
            .timestamp(LocalDateTime.now())
            .traceId(traceId)
            .build();
    }
}


