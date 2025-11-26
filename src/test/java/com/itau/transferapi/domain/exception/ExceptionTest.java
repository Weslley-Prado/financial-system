package com.itau.transferapi.domain.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Exception Tests")
class ExceptionTest {
    
    @Nested
    @DisplayName("ErrorCode Tests")
    class ErrorCodeTests {
        
        @Test
        @DisplayName("Todos os códigos devem ter prefixo ITAU-")
        void allCodesShouldHavePrefix() {
            for (ErrorCode code : ErrorCode.values()) {
                assertThat(code.getCode()).startsWith("ITAU-");
            }
        }
        
        @Test
        @DisplayName("Todos os códigos devem ter mensagem padrão")
        void allCodesShouldHaveDefaultMessage() {
            for (ErrorCode code : ErrorCode.values()) {
                assertThat(code.getDefaultMessage()).isNotNull();
                assertThat(code.getDefaultMessage()).isNotBlank();
            }
        }
        
        @Test
        @DisplayName("Todos os códigos devem ter status HTTP")
        void allCodesShouldHaveHttpStatus() {
            for (ErrorCode code : ErrorCode.values()) {
                assertThat(code.getHttpStatus()).isNotNull();
            }
        }
        
        @Test
        @DisplayName("Códigos de validação devem começar com 1")
        void validationCodesShouldStartWith1() {
            assertThat(ErrorCode.INVALID_REQUEST.getCode()).startsWith("ITAU-1");
            assertThat(ErrorCode.SAME_ACCOUNT_TRANSFER.getCode()).startsWith("ITAU-1");
            assertThat(ErrorCode.INVALID_AMOUNT.getCode()).startsWith("ITAU-1");
            assertThat(ErrorCode.INVALID_ACCOUNT.getCode()).startsWith("ITAU-1");
        }
        
        @Test
        @DisplayName("Códigos de negócio devem começar com 2")
        void businessCodesShouldStartWith2() {
            assertThat(ErrorCode.ACCOUNT_NOT_ACTIVE.getCode()).startsWith("ITAU-2");
            assertThat(ErrorCode.INSUFFICIENT_BALANCE.getCode()).startsWith("ITAU-2");
            assertThat(ErrorCode.INSUFFICIENT_LIMIT.getCode()).startsWith("ITAU-2");
            assertThat(ErrorCode.DAILY_LIMIT_EXCEEDED.getCode()).startsWith("ITAU-2");
            assertThat(ErrorCode.CLIENT_NOT_ACTIVE.getCode()).startsWith("ITAU-2");
        }
        
        @Test
        @DisplayName("Códigos de recurso não encontrado devem começar com 3")
        void notFoundCodesShouldStartWith3() {
            assertThat(ErrorCode.ACCOUNT_NOT_FOUND.getCode()).startsWith("ITAU-3");
            assertThat(ErrorCode.CLIENT_NOT_FOUND.getCode()).startsWith("ITAU-3");
            assertThat(ErrorCode.TRANSFER_NOT_FOUND.getCode()).startsWith("ITAU-3");
        }
        
        @Test
        @DisplayName("Códigos de integração devem começar com 4")
        void integrationCodesShouldStartWith4() {
            assertThat(ErrorCode.CADASTRO_API_ERROR.getCode()).startsWith("ITAU-4");
            assertThat(ErrorCode.BACEN_API_ERROR.getCode()).startsWith("ITAU-4");
            assertThat(ErrorCode.BACEN_RATE_LIMIT.getCode()).startsWith("ITAU-4");
        }
        
        @Test
        @DisplayName("Código de erro interno deve começar com 5")
        void internalErrorCodeShouldStartWith5() {
            assertThat(ErrorCode.INTERNAL_ERROR.getCode()).startsWith("ITAU-5");
            assertThat(ErrorCode.DATABASE_ERROR.getCode()).startsWith("ITAU-5");
        }
    }
    
    @Nested
    @DisplayName("BusinessException Tests")
    class BusinessExceptionTests {
        
        @Test
        @DisplayName("Deve criar exceção apenas com código")
        void shouldCreateWithCodeOnly() {
            BusinessException ex = new BusinessException(ErrorCode.INSUFFICIENT_BALANCE);
            
            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.INSUFFICIENT_BALANCE);
            assertThat(ex.getMessage()).isEqualTo(ErrorCode.INSUFFICIENT_BALANCE.getDefaultMessage());
            assertThat(ex.getDetails()).isNull();
        }
        
        @Test
        @DisplayName("Deve criar exceção com código e detalhes")
        void shouldCreateWithCodeAndDetails() {
            BusinessException ex = new BusinessException(
                ErrorCode.INSUFFICIENT_BALANCE, 
                "Saldo de R$ 50 é insuficiente para R$ 100"
            );
            
            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.INSUFFICIENT_BALANCE);
            assertThat(ex.getMessage()).isEqualTo("Saldo de R$ 50 é insuficiente para R$ 100");
            assertThat(ex.getDetails()).isEqualTo("Saldo de R$ 50 é insuficiente para R$ 100");
        }
        
        @Test
        @DisplayName("Deve criar exceção com código, detalhes e causa")
        void shouldCreateWithCodeDetailsAndCause() {
            RuntimeException cause = new RuntimeException("Original error");
            BusinessException ex = new BusinessException(
                ErrorCode.INTERNAL_ERROR, 
                "Erro processando transferência",
                cause
            );
            
            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.INTERNAL_ERROR);
            assertThat(ex.getCause()).isEqualTo(cause);
        }
        
        @Test
        @DisplayName("Deve formatar mensagem corretamente")
        void shouldFormatMessageCorrectly() {
            BusinessException ex = new BusinessException(
                ErrorCode.INSUFFICIENT_BALANCE, 
                "Detalhes do erro"
            );
            
            String formatted = ex.getFormattedMessage();
            
            assertThat(formatted).contains("ITAU-2002");
            assertThat(formatted).contains("Saldo insuficiente");
            assertThat(formatted).contains("Detalhes do erro");
        }
        
        @Test
        @DisplayName("Deve formatar mensagem sem detalhes")
        void shouldFormatMessageWithoutDetails() {
            BusinessException ex = new BusinessException(ErrorCode.SAME_ACCOUNT_TRANSFER);
            
            String formatted = ex.getFormattedMessage();
            
            assertThat(formatted).contains("ITAU-1004");
            assertThat(formatted).contains("mesma conta");
        }
    }
    
    @Nested
    @DisplayName("ResourceNotFoundException Tests")
    class ResourceNotFoundExceptionTests {
        
        @Test
        @DisplayName("Deve criar exceção de conta não encontrada")
        void shouldCreateAccountNotFound() {
            ResourceNotFoundException ex = ResourceNotFoundException.account("12345-6");
            
            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.ACCOUNT_NOT_FOUND);
            assertThat(ex.getResourceType()).isEqualTo("Conta");
            assertThat(ex.getResourceId()).isEqualTo("12345-6");
        }
        
        @Test
        @DisplayName("Deve criar exceção de cliente não encontrado")
        void shouldCreateClientNotFound() {
            String clientId = "550e8400-e29b-41d4-a716-446655440000";
            ResourceNotFoundException ex = ResourceNotFoundException.client(clientId);
            
            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.CLIENT_NOT_FOUND);
            assertThat(ex.getResourceType()).isEqualTo("Cliente");
            assertThat(ex.getResourceId()).isEqualTo(clientId);
        }
        
        @Test
        @DisplayName("Deve criar exceção de transferência não encontrada")
        void shouldCreateTransferNotFound() {
            String transferId = "550e8400-e29b-41d4-a716-446655440000";
            ResourceNotFoundException ex = ResourceNotFoundException.transfer(transferId);
            
            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.TRANSFER_NOT_FOUND);
            assertThat(ex.getResourceType()).isEqualTo("Transferência");
        }
    }
    
    @Nested
    @DisplayName("IntegrationException Tests")
    class IntegrationExceptionTests {
        
        @Test
        @DisplayName("Deve criar exceção de erro do Cadastro")
        void shouldCreateCadastroError() {
            IntegrationException ex = IntegrationException.cadastroError(
                "Timeout", 
                new RuntimeException("Connection timeout")
            );
            
            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.CADASTRO_API_ERROR);
            assertThat(ex.getMessage()).contains("Timeout");
            assertThat(ex.getCause()).isNotNull();
        }
        
        @Test
        @DisplayName("Deve criar exceção de erro do BACEN")
        void shouldCreateBacenError() {
            IntegrationException ex = IntegrationException.bacenError(
                "Service unavailable", 
                null
            );
            
            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.BACEN_API_ERROR);
        }
        
        @Test
        @DisplayName("Deve criar exceção de rate limit do BACEN")
        void shouldCreateBacenRateLimit() {
            IntegrationException ex = IntegrationException.bacenRateLimit();
            
            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.BACEN_RATE_LIMIT);
        }
        
        @Test
        @DisplayName("Deve criar exceção de BACEN indisponível")
        void shouldCreateBacenUnavailable() {
            IntegrationException ex = IntegrationException.bacenUnavailable(
                new RuntimeException("Service down")
            );
            
            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.BACEN_API_UNAVAILABLE);
        }
    }
}
