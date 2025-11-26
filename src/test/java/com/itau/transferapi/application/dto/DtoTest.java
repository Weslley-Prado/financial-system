package com.itau.transferapi.application.dto;

import com.itau.transferapi.application.dto.request.TransferRequest;
import com.itau.transferapi.application.dto.response.BalanceResponse;
import com.itau.transferapi.application.dto.response.ErrorResponse;
import com.itau.transferapi.application.dto.response.TransferResponse;
import com.itau.transferapi.domain.valueobject.TransferStatus;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("DTO Tests")
class DtoTest {
    
    private static Validator validator;
    
    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }
    
    @Nested
    @DisplayName("TransferRequest Tests")
    class TransferRequestTests {
        
        @Test
        @DisplayName("Deve criar request válido")
        void shouldCreateValidRequest() {
            TransferRequest request = new TransferRequest(
                "12345-6",
                "0001",
                "98765-4",
                "0002",
                new BigDecimal("100.00"),
                "Test transfer"
            );
            
            Set<ConstraintViolation<TransferRequest>> violations = validator.validate(request);
            
            assertThat(violations).isEmpty();
            assertThat(request.sourceAccountNumber()).isEqualTo("12345-6");
            assertThat(request.targetAccountNumber()).isEqualTo("98765-4");
            assertThat(request.amount()).isEqualByComparingTo(new BigDecimal("100.00"));
        }
        
        @Test
        @DisplayName("Deve falhar validação para conta origem nula")
        void shouldFailValidationForNullSourceAccount() {
            TransferRequest request = new TransferRequest(
                null,
                "0001",
                "98765-4",
                "0002",
                new BigDecimal("100.00"),
                null
            );
            
            Set<ConstraintViolation<TransferRequest>> violations = validator.validate(request);
            
            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("sourceAccountNumber"));
        }
        
        @Test
        @DisplayName("Deve falhar validação para valor negativo")
        void shouldFailValidationForNegativeAmount() {
            TransferRequest request = new TransferRequest(
                "12345-6",
                "0001",
                "98765-4",
                "0002",
                new BigDecimal("-100.00"),
                null
            );
            
            Set<ConstraintViolation<TransferRequest>> violations = validator.validate(request);
            
            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("amount"));
        }
        
        @Test
        @DisplayName("Deve falhar validação para valor zero")
        void shouldFailValidationForZeroAmount() {
            TransferRequest request = new TransferRequest(
                "12345-6",
                "0001",
                "98765-4",
                "0002",
                BigDecimal.ZERO,
                null
            );
            
            Set<ConstraintViolation<TransferRequest>> violations = validator.validate(request);
            
            assertThat(violations).isNotEmpty();
        }
        
        @Test
        @DisplayName("Deve falhar validação para valor acima do máximo")
        void shouldFailValidationForAmountAboveMax() {
            TransferRequest request = new TransferRequest(
                "12345-6",
                "0001",
                "98765-4",
                "0002",
                new BigDecimal("150000.00"),
                null
            );
            
            Set<ConstraintViolation<TransferRequest>> violations = validator.validate(request);
            
            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("amount"));
        }
        
        @Test
        @DisplayName("Deve aceitar descrição opcional")
        void shouldAcceptOptionalDescription() {
            TransferRequest request = new TransferRequest(
                "12345-6",
                "0001",
                "98765-4",
                "0002",
                new BigDecimal("100.00"),
                null
            );
            
            Set<ConstraintViolation<TransferRequest>> violations = validator.validate(request);
            
            assertThat(violations).isEmpty();
            assertThat(request.description()).isNull();
        }
    }
    
    @Nested
    @DisplayName("TransferResponse Tests")
    class TransferResponseTests {
        
        @Test
        @DisplayName("Deve criar response com builder")
        void shouldCreateResponseWithBuilder() {
            UUID transferId = UUID.randomUUID();
            BigDecimal amount = new BigDecimal("100.00");
            
            TransferResponse response = TransferResponse.builder()
                .transferId(transferId)
                .status(TransferStatus.BACEN_NOTIFIED)
                .amount(amount)
                .formattedAmount("R$ 100,00")
                .sourceAccountNumber("12345-6")
                .sourceAgencyNumber("0001")
                .targetAccountNumber("98765-4")
                .targetAgencyNumber("0002")
                .createdAt(LocalDateTime.now())
                .completedAt(LocalDateTime.now())
                .bacenNotificationId("BCN-123456")
                .message("Transferência realizada com sucesso")
                .build();
            
            assertThat(response.transferId()).isEqualTo(transferId);
            assertThat(response.status()).isEqualTo(TransferStatus.BACEN_NOTIFIED);
            assertThat(response.amount()).isEqualByComparingTo(amount);
            assertThat(response.formattedAmount()).contains("R$");
            assertThat(response.message()).isEqualTo("Transferência realizada com sucesso");
        }
        
        @Test
        @DisplayName("Deve criar response com BACEN pendente")
        void shouldCreateBacenPendingResponse() {
            UUID transferId = UUID.randomUUID();
            
            TransferResponse response = TransferResponse.builder()
                .transferId(transferId)
                .status(TransferStatus.BACEN_PENDING)
                .amount(new BigDecimal("100.00"))
                .formattedAmount("R$ 100,00")
                .sourceAccountNumber("12345-6")
                .sourceAgencyNumber("0001")
                .targetAccountNumber("98765-4")
                .targetAgencyNumber("0002")
                .createdAt(LocalDateTime.now())
                .completedAt(LocalDateTime.now())
                .message("Transferência realizada. Notificação ao BACEN pendente.")
                .build();
            
            assertThat(response.status()).isEqualTo(TransferStatus.BACEN_PENDING);
            assertThat(response.bacenNotificationId()).isNull();
            assertThat(response.message()).contains("BACEN pendente");
        }
    }
    
    @Nested
    @DisplayName("BalanceResponse Tests")
    class BalanceResponseTests {
        
        @Test
        @DisplayName("Deve criar response de saldo com builder")
        void shouldCreateBalanceResponseWithBuilder() {
            BalanceResponse response = BalanceResponse.builder()
                .accountNumber("12345-6")
                .agencyNumber("0001")
                .holderName("João Silva")
                .balance(new BigDecimal("5000.00"))
                .formattedBalance("R$ 5.000,00")
                .availableLimit(new BigDecimal("10000.00"))
                .formattedAvailableLimit("R$ 10.000,00")
                .dailyTransferLimitAvailable(new BigDecimal("1000.00"))
                .formattedDailyTransferLimit("R$ 1.000,00")
                .queryTime(LocalDateTime.now())
                .build();
            
            assertThat(response.accountNumber()).isEqualTo("12345-6");
            assertThat(response.agencyNumber()).isEqualTo("0001");
            assertThat(response.holderName()).isEqualTo("João Silva");
            assertThat(response.balance()).isEqualByComparingTo(new BigDecimal("5000.00"));
            assertThat(response.formattedBalance()).contains("R$");
            assertThat(response.availableLimit()).isEqualByComparingTo(new BigDecimal("10000.00"));
            assertThat(response.dailyTransferLimitAvailable()).isEqualByComparingTo(new BigDecimal("1000.00"));
            assertThat(response.queryTime()).isNotNull();
        }
    }
    
    @Nested
    @DisplayName("ErrorResponse Tests")
    class ErrorResponseTests {
        
        @Test
        @DisplayName("Deve criar response de erro simples")
        void shouldCreateSimpleErrorResponse() {
            ErrorResponse response = ErrorResponse.of(
                "ITAU-2002",
                "Saldo insuficiente",
                "/api/v1/transfers",
                "abc123"
            );
            
            assertThat(response.code()).isEqualTo("ITAU-2002");
            assertThat(response.message()).isEqualTo("Saldo insuficiente");
            assertThat(response.path()).isEqualTo("/api/v1/transfers");
            assertThat(response.traceId()).isEqualTo("abc123");
            assertThat(response.timestamp()).isNotNull();
            assertThat(response.details()).isNull();
            assertThat(response.fieldErrors()).isNull();
        }
        
        @Test
        @DisplayName("Deve criar response de erro com detalhes")
        void shouldCreateDetailedErrorResponse() {
            ErrorResponse response = ErrorResponse.of(
                "ITAU-2002",
                "Saldo insuficiente",
                "Saldo de R$ 50 é insuficiente para R$ 100",
                "/api/v1/transfers",
                "abc123"
            );
            
            assertThat(response.code()).isEqualTo("ITAU-2002");
            assertThat(response.message()).isEqualTo("Saldo insuficiente");
            assertThat(response.details()).isEqualTo("Saldo de R$ 50 é insuficiente para R$ 100");
        }
        
        @Test
        @DisplayName("Deve criar response de erro de validação com builder")
        void shouldCreateValidationErrorResponseWithBuilder() {
            List<ErrorResponse.FieldError> fieldErrors = List.of(
                ErrorResponse.FieldError.builder()
                    .field("amount")
                    .message("deve ser maior que 0")
                    .rejectedValue("-100")
                    .build(),
                ErrorResponse.FieldError.builder()
                    .field("sourceAccountNumber")
                    .message("não pode ser vazio")
                    .rejectedValue(null)
                    .build()
            );
            
            ErrorResponse response = ErrorResponse.builder()
                .code("ITAU-1001")
                .message("Requisição inválida")
                .path("/api/v1/transfers")
                .traceId("xyz789")
                .timestamp(LocalDateTime.now())
                .fieldErrors(fieldErrors)
                .build();
            
            assertThat(response.code()).isEqualTo("ITAU-1001");
            assertThat(response.fieldErrors()).hasSize(2);
            assertThat(response.fieldErrors().get(0).field()).isEqualTo("amount");
            assertThat(response.fieldErrors().get(1).field()).isEqualTo("sourceAccountNumber");
        }
    }
}
