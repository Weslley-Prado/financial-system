package com.itau.transferapi.domain.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Enumeração centralizada de códigos de erro da aplicação.
 * 
 * Padronização:
 * - ITAU-XXXX: Código único para rastreabilidade
 * - Mapeamento HTTP correto para cada erro
 * - Mensagens claras para o cliente
 */
@Getter
public enum ErrorCode {
    
    // Erros de validação (400)
    INVALID_REQUEST("ITAU-1001", "Requisição inválida", HttpStatus.BAD_REQUEST),
    INVALID_AMOUNT("ITAU-1002", "Valor inválido para transferência", HttpStatus.BAD_REQUEST),
    INVALID_ACCOUNT("ITAU-1003", "Dados da conta inválidos", HttpStatus.BAD_REQUEST),
    SAME_ACCOUNT_TRANSFER("ITAU-1004", "Não é permitido transferir para a mesma conta", HttpStatus.BAD_REQUEST),
    
    // Erros de negócio (422)
    ACCOUNT_NOT_ACTIVE("ITAU-2001", "Conta não está ativa", HttpStatus.UNPROCESSABLE_ENTITY),
    INSUFFICIENT_BALANCE("ITAU-2002", "Saldo insuficiente", HttpStatus.UNPROCESSABLE_ENTITY),
    INSUFFICIENT_LIMIT("ITAU-2003", "Limite disponível insuficiente", HttpStatus.UNPROCESSABLE_ENTITY),
    DAILY_LIMIT_EXCEEDED("ITAU-2004", "Limite diário de transferência excedido", HttpStatus.UNPROCESSABLE_ENTITY),
    CLIENT_NOT_ACTIVE("ITAU-2005", "Cliente não está ativo", HttpStatus.UNPROCESSABLE_ENTITY),
    TRANSFER_NOT_ALLOWED("ITAU-2006", "Transferência não permitida", HttpStatus.UNPROCESSABLE_ENTITY),
    
    // Erros de recurso não encontrado (404)
    ACCOUNT_NOT_FOUND("ITAU-3001", "Conta não encontrada", HttpStatus.NOT_FOUND),
    CLIENT_NOT_FOUND("ITAU-3002", "Cliente não encontrado", HttpStatus.NOT_FOUND),
    TRANSFER_NOT_FOUND("ITAU-3003", "Transferência não encontrada", HttpStatus.NOT_FOUND),
    
    // Erros de integração (502/503)
    CADASTRO_API_ERROR("ITAU-4001", "Erro ao consultar API de Cadastro", HttpStatus.BAD_GATEWAY),
    CADASTRO_API_UNAVAILABLE("ITAU-4002", "API de Cadastro indisponível", HttpStatus.SERVICE_UNAVAILABLE),
    BACEN_API_ERROR("ITAU-4003", "Erro ao notificar BACEN", HttpStatus.BAD_GATEWAY),
    BACEN_API_UNAVAILABLE("ITAU-4004", "API do BACEN indisponível", HttpStatus.SERVICE_UNAVAILABLE),
    BACEN_RATE_LIMIT("ITAU-4005", "Rate limit do BACEN excedido", HttpStatus.TOO_MANY_REQUESTS),
    
    // Erros internos (500)
    INTERNAL_ERROR("ITAU-5001", "Erro interno do servidor", HttpStatus.INTERNAL_SERVER_ERROR),
    DATABASE_ERROR("ITAU-5002", "Erro ao acessar banco de dados", HttpStatus.INTERNAL_SERVER_ERROR),
    CONCURRENT_MODIFICATION("ITAU-5003", "Conflito de atualização concorrente", HttpStatus.CONFLICT);
    
    private final String code;
    private final String defaultMessage;
    private final HttpStatus httpStatus;
    
    ErrorCode(String code, String defaultMessage, HttpStatus httpStatus) {
        this.code = code;
        this.defaultMessage = defaultMessage;
        this.httpStatus = httpStatus;
    }
}


