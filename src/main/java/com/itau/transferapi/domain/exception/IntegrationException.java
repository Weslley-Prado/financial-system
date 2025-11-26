package com.itau.transferapi.domain.exception;

import lombok.Getter;

/**
 * Exceção para erros de integração com sistemas externos.
 * 
 * Utilizada para:
 * - Falhas na API de Cadastro
 * - Falhas na API do BACEN
 * - Timeouts de conexão
 * - Rate limiting
 */
@Getter
public class IntegrationException extends RuntimeException {
    
    private final ErrorCode errorCode;
    private final String serviceName;
    private final boolean retryable;
    
    public IntegrationException(ErrorCode errorCode, String serviceName, String message) {
        super(message);
        this.errorCode = errorCode;
        this.serviceName = serviceName;
        this.retryable = determineRetryable(errorCode);
    }
    
    public IntegrationException(ErrorCode errorCode, String serviceName, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.serviceName = serviceName;
        this.retryable = determineRetryable(errorCode);
    }
    
    private boolean determineRetryable(ErrorCode errorCode) {
        return switch (errorCode) {
            case BACEN_RATE_LIMIT, CADASTRO_API_UNAVAILABLE, BACEN_API_UNAVAILABLE -> true;
            default -> false;
        };
    }
    
    public static IntegrationException cadastroUnavailable(Throwable cause) {
        return new IntegrationException(
            ErrorCode.CADASTRO_API_UNAVAILABLE,
            "Cadastro API",
            "Serviço de Cadastro temporariamente indisponível",
            cause
        );
    }
    
    public static IntegrationException cadastroError(String message, Throwable cause) {
        return new IntegrationException(
            ErrorCode.CADASTRO_API_ERROR,
            "Cadastro API",
            message,
            cause
        );
    }
    
    public static IntegrationException bacenUnavailable(Throwable cause) {
        return new IntegrationException(
            ErrorCode.BACEN_API_UNAVAILABLE,
            "BACEN API",
            "Serviço do BACEN temporariamente indisponível",
            cause
        );
    }
    
    public static IntegrationException bacenRateLimit() {
        return new IntegrationException(
            ErrorCode.BACEN_RATE_LIMIT,
            "BACEN API",
            "Rate limit do BACEN excedido. Tentativa será reagendada."
        );
    }
    
    public static IntegrationException bacenError(String message, Throwable cause) {
        return new IntegrationException(
            ErrorCode.BACEN_API_ERROR,
            "BACEN API",
            message,
            cause
        );
    }
}


