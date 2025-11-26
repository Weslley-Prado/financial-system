package com.itau.transferapi.web.exception;

import com.itau.transferapi.application.dto.response.ErrorResponse;
import com.itau.transferapi.domain.exception.BusinessException;
import com.itau.transferapi.domain.exception.ErrorCode;
import com.itau.transferapi.domain.exception.IntegrationException;
import com.itau.transferapi.domain.exception.ResourceNotFoundException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Handler global de exceções para padronização de respostas de erro.
 * 
 * Mapeia exceções para respostas HTTP apropriadas com
 * códigos de erro e mensagens padronizadas.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException ex, HttpServletRequest request) {
        
        String traceId = generateTraceId();
        log.warn("[{}] Erro de negócio: {}", traceId, ex.getFormattedMessage());
        
        ErrorResponse response = ErrorResponse.builder()
            .code(ex.getErrorCode().getCode())
            .message(ex.getErrorCode().getDefaultMessage())
            .details(ex.getDetails())
            .path(request.getRequestURI())
            .timestamp(LocalDateTime.now())
            .traceId(traceId)
            .build();
        
        return ResponseEntity
            .status(ex.getErrorCode().getHttpStatus())
            .body(response);
    }
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex, HttpServletRequest request) {
        
        String traceId = generateTraceId();
        log.warn("[{}] Recurso não encontrado: {} - {}", 
            traceId, ex.getResourceType(), ex.getResourceId());
        
        ErrorResponse response = ErrorResponse.builder()
            .code(ex.getErrorCode().getCode())
            .message(ex.getErrorCode().getDefaultMessage())
            .details(ex.getMessage())
            .path(request.getRequestURI())
            .timestamp(LocalDateTime.now())
            .traceId(traceId)
            .build();
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
    
    @ExceptionHandler(IntegrationException.class)
    public ResponseEntity<ErrorResponse> handleIntegrationException(
            IntegrationException ex, HttpServletRequest request) {
        
        String traceId = generateTraceId();
        log.error("[{}] Erro de integração com {}: {}", 
            traceId, ex.getServiceName(), ex.getMessage(), ex);
        
        ErrorResponse response = ErrorResponse.builder()
            .code(ex.getErrorCode().getCode())
            .message(ex.getErrorCode().getDefaultMessage())
            .details(ex.isRetryable() ? 
                "Serviço temporariamente indisponível. Tente novamente em alguns instantes." : 
                ex.getMessage())
            .path(request.getRequestURI())
            .timestamp(LocalDateTime.now())
            .traceId(traceId)
            .build();
        
        return ResponseEntity
            .status(ex.getErrorCode().getHttpStatus())
            .body(response);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        
        String traceId = generateTraceId();
        
        List<ErrorResponse.FieldError> fieldErrors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> ErrorResponse.FieldError.builder()
                .field(error.getField())
                .message(error.getDefaultMessage())
                .rejectedValue(error.getRejectedValue())
                .build())
            .collect(Collectors.toList());
        
        log.warn("[{}] Erro de validação: {} erros", traceId, fieldErrors.size());
        
        ErrorResponse response = ErrorResponse.builder()
            .code(ErrorCode.INVALID_REQUEST.getCode())
            .message("Dados inválidos na requisição")
            .fieldErrors(fieldErrors)
            .path(request.getRequestURI())
            .timestamp(LocalDateTime.now())
            .traceId(traceId)
            .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(
            ConstraintViolationException ex, HttpServletRequest request) {
        
        String traceId = generateTraceId();
        
        List<ErrorResponse.FieldError> fieldErrors = ex.getConstraintViolations()
            .stream()
            .map(violation -> ErrorResponse.FieldError.builder()
                .field(violation.getPropertyPath().toString())
                .message(violation.getMessage())
                .rejectedValue(violation.getInvalidValue())
                .build())
            .collect(Collectors.toList());
        
        log.warn("[{}] Erro de validação de parâmetros: {} erros", traceId, fieldErrors.size());
        
        ErrorResponse response = ErrorResponse.builder()
            .code(ErrorCode.INVALID_REQUEST.getCode())
            .message("Parâmetros inválidos")
            .fieldErrors(fieldErrors)
            .path(request.getRequestURI())
            .timestamp(LocalDateTime.now())
            .traceId(traceId)
            .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    @ExceptionHandler(CallNotPermittedException.class)
    public ResponseEntity<ErrorResponse> handleCircuitBreakerException(
            CallNotPermittedException ex, HttpServletRequest request) {
        
        String traceId = generateTraceId();
        log.error("[{}] Circuit breaker aberto: {}", traceId, ex.getMessage());
        
        ErrorResponse response = ErrorResponse.builder()
            .code(ErrorCode.INTERNAL_ERROR.getCode())
            .message("Serviço temporariamente indisponível")
            .details("Sistema em modo de proteção. Tente novamente em alguns instantes.")
            .path(request.getRequestURI())
            .timestamp(LocalDateTime.now())
            .traceId(traceId)
            .build();
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
    
    @ExceptionHandler(RequestNotPermitted.class)
    public ResponseEntity<ErrorResponse> handleRateLimiterException(
            RequestNotPermitted ex, HttpServletRequest request) {
        
        String traceId = generateTraceId();
        log.warn("[{}] Rate limit excedido: {}", traceId, ex.getMessage());
        
        ErrorResponse response = ErrorResponse.builder()
            .code(ErrorCode.BACEN_RATE_LIMIT.getCode())
            .message("Limite de requisições excedido")
            .details("Aguarde alguns instantes antes de tentar novamente.")
            .path(request.getRequestURI())
            .timestamp(LocalDateTime.now())
            .traceId(traceId)
            .build();
        
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(response);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {
        
        String traceId = generateTraceId();
        log.error("[{}] Erro interno não tratado: {}", traceId, ex.getMessage(), ex);
        
        ErrorResponse response = ErrorResponse.builder()
            .code(ErrorCode.INTERNAL_ERROR.getCode())
            .message("Erro interno do servidor")
            .details("Ocorreu um erro inesperado. Por favor, tente novamente.")
            .path(request.getRequestURI())
            .timestamp(LocalDateTime.now())
            .traceId(traceId)
            .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
    
    private String generateTraceId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}


