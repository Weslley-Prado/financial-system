package com.itau.transferapi.domain.exception;

import lombok.Getter;

/**
 * Exceção para recursos não encontrados.
 */
@Getter
public class ResourceNotFoundException extends BusinessException {
    
    private final String resourceType;
    private final String resourceId;
    
    public ResourceNotFoundException(ErrorCode errorCode, String resourceType, String resourceId) {
        super(errorCode, String.format("%s não encontrado: %s", resourceType, resourceId));
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }
    
    public static ResourceNotFoundException account(String accountId) {
        return new ResourceNotFoundException(ErrorCode.ACCOUNT_NOT_FOUND, "Conta", accountId);
    }
    
    public static ResourceNotFoundException client(String clientId) {
        return new ResourceNotFoundException(ErrorCode.CLIENT_NOT_FOUND, "Cliente", clientId);
    }
    
    public static ResourceNotFoundException transfer(String transferId) {
        return new ResourceNotFoundException(ErrorCode.TRANSFER_NOT_FOUND, "Transferência", transferId);
    }
}


