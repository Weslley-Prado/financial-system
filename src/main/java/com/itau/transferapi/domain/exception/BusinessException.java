package com.itau.transferapi.domain.exception;

import lombok.Getter;

/**
 * Exceção base para erros de negócio da aplicação.
 * 
 * Esta exceção é utilizada para:
 * - Violações de regras de negócio
 * - Validações de domínio
 * - Condições de erro esperadas
 * 
 * Não deve ser utilizada para erros técnicos/infraestrutura.
 */
@Getter
public class BusinessException extends RuntimeException {
    
    private final ErrorCode errorCode;
    private final String details;
    
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
        this.details = null;
    }
    
    public BusinessException(ErrorCode errorCode, String details) {
        super(details != null ? details : errorCode.getDefaultMessage());
        this.errorCode = errorCode;
        this.details = details;
    }
    
    public BusinessException(ErrorCode errorCode, String details, Throwable cause) {
        super(details != null ? details : errorCode.getDefaultMessage(), cause);
        this.errorCode = errorCode;
        this.details = details;
    }
    
    /**
     * Retorna a mensagem completa formatada.
     * 
     * @return mensagem formatada
     */
    public String getFormattedMessage() {
        if (details != null) {
            return String.format("[%s] %s - %s", 
                errorCode.getCode(), 
                errorCode.getDefaultMessage(), 
                details);
        }
        return String.format("[%s] %s", 
            errorCode.getCode(), 
            errorCode.getDefaultMessage());
    }
}


