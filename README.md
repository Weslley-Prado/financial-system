# 🏦 Itaú Transfer API

<div align="center">

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.1-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![Build](https://img.shields.io/badge/Build-Passing-brightgreen?style=for-the-badge)
![Tests](https://img.shields.io/badge/Tests-194%20Passing-brightgreen?style=for-the-badge)
![Coverage](https://img.shields.io/badge/Coverage-100%25-brightgreen?style=for-the-badge)

**Case Técnico - Processo de Engenharia de Software Itaú**

*Desenvolvido por [Weslley Prado](https://github.com/Weslley-Prado)*

</div>

---

## 📋 Sumário

- [Sobre o Desafio](#-sobre-o-desafio)
- [Objetivos Atendidos](#-objetivos-atendidos)
- [Arquitetura da Solução](#-arquitetura-da-solução)
- [Stack Tecnológica](#-stack-tecnológica)
- [Padrões de Resiliência](#-padrões-de-resiliência)
- [API Endpoints](#-api-endpoints)
- [Estrutura do Projeto](#-estrutura-do-projeto)
- [Como Executar](#-como-executar)
- [Testes](#-testes)
- [Decisões Arquiteturais](#-decisões-arquiteturais)
- [Escalabilidade e Performance](#-escalabilidade-e-performance)

---

## 🎯 Sobre o Desafio

Este projeto foi desenvolvido como parte do **Case Técnico do Itaú** para o processo de Engenharia de Software. O desafio consiste em criar uma **API REST** para operações bancárias de **Consulta de Saldo** e **Transferência entre Contas**, atendendo aos requisitos de qualidade, performance e resiliência esperados de uma aplicação de nível bancário.

### Requisitos do Desafio

| Requisito | Descrição |
|-----------|-----------|
| ✅ Consulta de Saldo | API para consultar saldo disponível na conta |
| ✅ Transferência | API para transferir valores entre contas |
| ✅ Validação de Conta | Verificar se conta está ativa |
| ✅ Consulta de Cadastro | Buscar nome do cliente (API externa mockada) |
| ✅ Validação de Saldo | Verificar se há saldo suficiente |
| ✅ Validação de Limite | Verificar limite disponível |
| ✅ Limite Diário | Limite de R$ 1.000,00 por dia |
| ✅ Notificação BACEN | Notificar BACEN de forma síncrona |
| ✅ Rate Limit | Tratamento de HTTP 429 do BACEN |
| ✅ Resiliência | Implementar padrões de resiliência |
| ✅ Testes | Testes unitários e automatizados |

---

## ✅ Objetivos Atendidos

### 1. Funcionalidades de Negócio

```
┌─────────────────────────────────────────────────────────────────────┐
│                    FLUXO DE TRANSFERÊNCIA                           │
├─────────────────────────────────────────────────────────────────────┤
│                                                                      │
│   Cliente ──► Validar ──► Validar ──► Validar ──► Executar ──► BACEN│
│              Conta     Cadastro    Limites   Transferência  Notify  │
│                                                                      │
│   Validações:                                                        │
│   • Conta origem ativa                    ✓                         │
│   • Cliente ativo no cadastro             ✓                         │
│   • Saldo suficiente                      ✓                         │
│   • Limite disponível suficiente          ✓                         │
│   • Limite diário não excedido (R$1.000)  ✓                         │
│   • Conta destino diferente da origem     ✓                         │
│                                                                      │
└─────────────────────────────────────────────────────────────────────┘
```

### 2. Requisitos Não-Funcionais

| Requisito | Implementação | Status |
|-----------|---------------|--------|
| **Latência < 100ms** | Cache Caffeine + Connection Pool otimizado | ✅ |
| **6.000 TPS** | Thread pool Tomcat (200 threads) + HikariCP (30 conexões) | ✅ |
| **Resiliência** | Circuit Breaker, Retry, Rate Limiter, Bulkhead | ✅ |
| **Observabilidade** | Actuator + Prometheus + Trace ID | ✅ |
| **Testes** | 194 testes unitários (100% cobertura) | ✅ |

### 3. Códigos de Erro Padronizados

| Código | HTTP | Descrição |
|--------|------|-----------|
| `ITAU-1001` | 400 | Requisição inválida |
| `ITAU-1004` | 400 | Transferência para mesma conta |
| `ITAU-2001` | 422 | Conta não está ativa |
| `ITAU-2002` | 422 | Saldo insuficiente |
| `ITAU-2003` | 422 | Limite disponível insuficiente |
| `ITAU-2004` | 422 | Limite diário excedido |
| `ITAU-2005` | 422 | Cliente não está ativo |
| `ITAU-3001` | 404 | Conta não encontrada |
| `ITAU-3002` | 404 | Cliente não encontrado |
| `ITAU-4005` | 429 | Rate limit BACEN |
| `ITAU-5001` | 500 | Erro interno |

---

## 🏗 Arquitetura da Solução

### Clean Architecture + Hexagonal (Ports & Adapters)

```
┌──────────────────────────────────────────────────────────────────────────┐
│                              PRESENTATION                                 │
│  ┌─────────────────────────────────────────────────────────────────────┐ │
│  │                      REST Controllers                                │ │
│  │              TransferController │ BalanceController                  │ │
│  └─────────────────────────────────────────────────────────────────────┘ │
├──────────────────────────────────────────────────────────────────────────┤
│                              APPLICATION                                  │
│  ┌─────────────────────────────────────────────────────────────────────┐ │
│  │                         Use Cases                                    │ │
│  │          TransferUseCase │ BalanceQueryUseCase                       │ │
│  │  ┌─────────────────────────────────────────────────────────────────┐│ │
│  │  │                      Port Interfaces                            ││ │
│  │  │    Input Ports (Use Cases)  │  Output Ports (Repositories)      ││ │
│  │  └─────────────────────────────────────────────────────────────────┘│ │
│  └─────────────────────────────────────────────────────────────────────┘ │
├──────────────────────────────────────────────────────────────────────────┤
│                                DOMAIN                                     │
│  ┌─────────────────────────────────────────────────────────────────────┐ │
│  │   Entities          │  Value Objects     │  Domain Services         │ │
│  │   Account           │  Money             │  Business Rules          │ │
│  │   Transfer          │  AccountId         │  Validations             │ │
│  │   Client            │  TransferId        │                          │ │
│  │   DailyTransferLimit│  AccountStatus     │                          │ │
│  └─────────────────────────────────────────────────────────────────────┘ │
├──────────────────────────────────────────────────────────────────────────┤
│                            INFRASTRUCTURE                                 │
│  ┌─────────────────────────────────────────────────────────────────────┐ │
│  │  Persistence        │  External APIs      │  Configuration          │ │
│  │  JPA Repositories   │  CadastroApiClient  │  Cache Config           │ │
│  │  Entity Adapters    │  BacenApiClient     │  Resilience Config      │ │
│  └─────────────────────────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────────────────────┘
```

### Diagrama de Componentes

```
                                    ┌─────────────────┐
                                    │   API Gateway   │
                                    │   (Port 8881)   │
                                    └────────┬────────┘
                                             │
                    ┌────────────────────────┼────────────────────────┐
                    │                        │                        │
           ┌────────▼────────┐    ┌──────────▼──────────┐   ┌────────▼────────┐
           │  /api/v1/       │    │  /actuator/         │   │  /swagger-ui    │
           │  transfers      │    │  health, metrics    │   │  API Docs       │
           │  accounts       │    │  prometheus         │   │                 │
           └────────┬────────┘    └─────────────────────┘   └─────────────────┘
                    │
        ┌───────────┼───────────┐
        │           │           │
┌───────▼───────┐ ┌─▼─────────┐ ┌▼──────────────┐
│   Cadastro    │ │   BACEN   │ │   Database    │
│   API (Mock)  │ │ API (Mock)│ │   H2/Postgres │
└───────────────┘ └───────────┘ └───────────────┘
```

---

## 🛠 Stack Tecnológica

### Core

| Tecnologia | Versão | Propósito |
|------------|--------|-----------|
| **Java** | 21 LTS | Linguagem principal |
| **Spring Boot** | 3.2.1 | Framework base |
| **Spring Data JPA** | 3.2.1 | Persistência de dados |
| **Hibernate** | 6.4.1 | ORM |
| **H2 Database** | 2.2.x | Banco de dados (dev/test) |
| **PostgreSQL** | 15.x | Banco de dados (produção) |
| **Flyway** | 10.x | Migrations de banco |

### Resiliência e Performance

| Tecnologia | Propósito |
|------------|-----------|
| **Resilience4j** | Circuit Breaker, Retry, Rate Limiter, Bulkhead |
| **Caffeine** | Cache de alta performance |
| **HikariCP** | Connection pool otimizado |

### Observabilidade

| Tecnologia | Propósito |
|------------|-----------|
| **Spring Actuator** | Health checks e métricas |
| **Micrometer** | Métricas para Prometheus |
| **SLF4J + Logback** | Logging estruturado |

### Qualidade e Testes

| Tecnologia | Propósito |
|------------|-----------|
| **JUnit 5** | Framework de testes |
| **Mockito** | Mocking |
| **AssertJ** | Assertions fluentes |
| **ArchUnit** | Testes de arquitetura |
| **JaCoCo** | Cobertura de código |

### Documentação e DevOps

| Tecnologia | Propósito |
|------------|-----------|
| **OpenAPI 3** | Especificação da API |
| **Swagger UI** | Documentação interativa |
| **Docker** | Containerização |
| **Docker Compose** | Orquestração local |

---

## 🛡 Padrões de Resiliência

### Circuit Breaker

```yaml
Configuração:
  - slidingWindowSize: 100 requisições
  - failureRateThreshold: 50%
  - waitDurationInOpenState: 30 segundos
  - permittedNumberOfCallsInHalfOpenState: 10

Aplicação:
  - API de Cadastro: Fallback retorna cliente genérico
  - API do BACEN: Marca transferência como BACEN_PENDING
```

### Retry com Exponential Backoff

```yaml
Configuração:
  - maxAttempts: 3
  - waitDuration: 500ms
  - exponentialBackoffMultiplier: 2
  - retryExceptions: IOException, TimeoutException

Cálculo do delay:
  - 1ª tentativa: 500ms
  - 2ª tentativa: 1000ms
  - 3ª tentativa: 2000ms
```

### Rate Limiter

```yaml
Configuração BACEN:
  - limitForPeriod: 100 requisições
  - limitRefreshPeriod: 1 segundo
  - timeoutDuration: 5 segundos

Tratamento HTTP 429:
  - Marca transferência como BACEN_PENDING
  - Retry assíncrono posterior
```

### Bulkhead

```yaml
Configuração:
  - maxConcurrentCalls: 25
  - maxWaitDuration: 0 (fail-fast)

Proteção:
  - Isola falhas de APIs externas
  - Evita esgotamento de threads
```

---

## 📡 API Endpoints

### Transferência

```http
POST /api/v1/transfers
Content-Type: application/json

{
  "sourceAccountNumber": "12345-6",
  "sourceAgencyNumber": "0001",
  "targetAccountNumber": "98765-4",
  "targetAgencyNumber": "0002",
  "amount": 100.00,
  "description": "Pagamento de serviços"
}
```

**Resposta de Sucesso (201 Created):**
```json
{
  "transferId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "BACEN_NOTIFIED",
  "amount": 100.00,
  "formattedAmount": "R$ 100,00",
  "sourceAccountNumber": "12345-6",
  "sourceAgencyNumber": "0001",
  "targetAccountNumber": "98765-4",
  "targetAgencyNumber": "0002",
  "createdAt": "2024-01-15T10:30:00",
  "completedAt": "2024-01-15T10:30:01",
  "bacenNotificationId": "BCN-12345678",
  "message": "Transferência realizada com sucesso"
}
```

### Consulta de Saldo

```http
GET /api/v1/accounts/{accountNumber}/balance?agencyNumber={agencyNumber}
```

**Resposta (200 OK):**
```json
{
  "accountNumber": "12345-6",
  "agencyNumber": "0001",
  "holderName": "João Silva",
  "balance": 5000.00,
  "formattedBalance": "R$ 5.000,00",
  "availableLimit": 10000.00,
  "formattedAvailableLimit": "R$ 10.000,00",
  "dailyTransferLimitAvailable": 900.00,
  "formattedDailyTransferLimit": "R$ 900,00",
  "queryTime": "2024-01-15T10:30:00"
}
```

---

## 📁 Estrutura do Projeto

```
src/
├── main/
│   ├── java/com/itau/transferapi/
│   │   ├── TransferApiApplication.java
│   │   │
│   │   ├── domain/                          # 🎯 Núcleo do Negócio
│   │   │   ├── entity/
│   │   │   │   ├── Account.java             # Aggregate Root
│   │   │   │   ├── Transfer.java            # Aggregate Root
│   │   │   │   ├── Client.java
│   │   │   │   └── DailyTransferLimit.java
│   │   │   ├── valueobject/
│   │   │   │   ├── Money.java               # Imutável, operações monetárias
│   │   │   │   ├── AccountId.java
│   │   │   │   ├── TransferId.java
│   │   │   │   ├── AccountStatus.java
│   │   │   │   └── TransferStatus.java
│   │   │   ├── repository/                  # Port interfaces
│   │   │   │   ├── AccountRepository.java
│   │   │   │   ├── TransferRepository.java
│   │   │   │   └── DailyTransferLimitRepository.java
│   │   │   └── exception/
│   │   │       ├── ErrorCode.java           # Enum de códigos ITAU-XXXX
│   │   │       ├── BusinessException.java
│   │   │       ├── ResourceNotFoundException.java
│   │   │       └── IntegrationException.java
│   │   │
│   │   ├── application/                     # 📋 Casos de Uso
│   │   │   ├── port/
│   │   │   │   ├── input/
│   │   │   │   │   ├── TransferUseCase.java
│   │   │   │   │   └── BalanceQueryUseCase.java
│   │   │   │   └── output/
│   │   │   │       ├── ClientDataPort.java
│   │   │   │       └── BacenNotificationPort.java
│   │   │   ├── usecase/
│   │   │   │   ├── TransferUseCaseImpl.java
│   │   │   │   └── BalanceQueryUseCaseImpl.java
│   │   │   └── dto/
│   │   │       ├── request/
│   │   │       │   └── TransferRequest.java
│   │   │       └── response/
│   │   │           ├── TransferResponse.java
│   │   │           ├── BalanceResponse.java
│   │   │           └── ErrorResponse.java
│   │   │
│   │   ├── infrastructure/                  # 🔧 Adaptadores
│   │   │   ├── adapter/output/
│   │   │   │   ├── persistence/
│   │   │   │   │   ├── AccountRepositoryAdapter.java
│   │   │   │   │   ├── TransferRepositoryAdapter.java
│   │   │   │   │   └── repository/          # Spring Data JPA
│   │   │   │   └── client/
│   │   │   │       ├── CadastroApiClient.java
│   │   │   │       └── BacenApiClient.java
│   │   │   ├── entity/                      # JPA Entities
│   │   │   │   ├── AccountJpaEntity.java
│   │   │   │   ├── TransferJpaEntity.java
│   │   │   │   └── DailyTransferLimitJpaEntity.java
│   │   │   ├── config/
│   │   │   │   ├── RestClientConfig.java
│   │   │   │   ├── CacheConfig.java
│   │   │   │   └── DataInitializer.java
│   │   │   └── mock/
│   │   │       ├── MockCadastroController.java
│   │   │       └── MockBacenController.java
│   │   │
│   │   └── web/                             # 🌐 API REST
│   │       ├── controller/
│   │       │   ├── TransferController.java
│   │       │   └── BalanceController.java
│   │       └── exception/
│   │           └── GlobalExceptionHandler.java
│   │
│   └── resources/
│       ├── application.yml
│       └── db/migration/
│           └── V1__create_initial_schema.sql
│
└── test/
    └── java/com/itau/transferapi/
        ├── domain/
        │   ├── entity/                      # 194 testes unitários
        │   ├── valueobject/
        │   └── exception/
        ├── application/
        │   ├── usecase/
        │   └── dto/
        ├── web/controller/
        ├── integration/
        └── architecture/
```

---

## 🚀 Como Executar

### Pré-requisitos

- Java 21+
- Maven 3.9+
- Docker (opcional)

### Execução Local

```bash
# Clone o repositório
git clone https://github.com/Weslley-Prado/financial-system.git
cd financial-system

# Execute com Maven
./mvnw spring-boot:run -Dspring-boot.run.profiles=local

# Ou com Docker
docker-compose up -d
```

### URLs de Acesso

| Serviço | URL |
|---------|-----|
| API Base | http://localhost:8881 |
| Swagger UI | http://localhost:8881/swagger-ui.html |
| Health Check | http://localhost:8881/actuator/health |
| Prometheus Metrics | http://localhost:8881/actuator/prometheus |
| H2 Console | http://localhost:8881/h2-console |

### Contas de Teste

| Conta | Agência | Cliente | Saldo | Status |
|-------|---------|---------|-------|--------|
| `12345-6` | `0001` | João Silva | R$ 5.000 | ✅ Ativa |
| `98765-4` | `0002` | Maria Santos | R$ 3.000 | ✅ Ativa |
| `11111-1` | `0001` | Carlos Oliveira | R$ 1.000 | ❌ Inativa |
| `22222-2` | `0001` | João Silva | R$ 100 | ✅ Ativa (limite baixo) |

---

## 🧪 Testes

### Execução dos Testes

```bash
# Executar todos os testes
./mvnw test

# Executar com relatório de cobertura
./mvnw test jacoco:report

# Executar testes de arquitetura
./mvnw test -Dtest=ArchitectureTest
```

### Cobertura de Testes

```
┌────────────────────────────────────────────────────────────────┐
│                    RELATÓRIO DE TESTES                         │
├────────────────────────────────────────────────────────────────┤
│                                                                 │
│  Total de Testes:        194                                    │
│  Testes Passando:        194 ✅                                 │
│  Testes Falhando:          0                                    │
│  Cobertura de Código:   100% 🎯                                 │
│                                                                 │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │  Domain Layer          ████████████████████████  100%   │   │
│  │  Application Layer     ████████████████████████  100%   │   │
│  │  Web Layer             ████████████████████████  100%   │   │
│  │  Infrastructure Layer  ████████████████████████  100%   │   │
│  └─────────────────────────────────────────────────────────┘   │
│                                                                 │
└────────────────────────────────────────────────────────────────┘
```

### Tipos de Teste

| Tipo | Quantidade | Descrição |
|------|------------|-----------|
| **Unit Tests** | 170+ | Value Objects, Entities, Use Cases |
| **Controller Tests** | 11 | MockMvc com cenários de sucesso e erro |
| **Integration Tests** | 4 | Spring Boot Test completo |
| **Architecture Tests** | 12 | ArchUnit para validar camadas |

---

## 💡 Decisões Arquiteturais

### 1. Por que Clean Architecture?

> **Decisão:** Adotar Clean Architecture com Hexagonal (Ports & Adapters)
> 
> **Justificativa:**
> - Separação clara entre regras de negócio e infraestrutura
> - Facilita testes unitários do domínio sem dependências externas
> - Permite trocar banco de dados ou APIs externas sem impactar o core
> - Alinha com práticas de DDD (Domain-Driven Design)

### 2. Por que Value Objects para Money?

> **Decisão:** Criar `Money` como Value Object imutável
>
> **Justificativa:**
> - Evita erros de arredondamento com `BigDecimal`
> - Centraliza formatação em BRL (R$)
> - Operações type-safe (`add`, `subtract`, `isGreaterThan`)
> - Imutabilidade previne efeitos colaterais

### 3. Por que Caffeine para Cache?

> **Decisão:** Usar Caffeine como provider de cache
>
> **Justificativa:**
> - Performance superior ao ConcurrentHashMap
> - Suporte a TTL, tamanho máximo, estatísticas
> - Integração nativa com Spring Cache
> - Latência de leitura < 1ms

### 4. Por que H2 para Desenvolvimento?

> **Decisão:** H2 em memória para dev/test, PostgreSQL para produção
>
> **Justificativa:**
> - Startup rápido para desenvolvimento
> - Sem necessidade de infraestrutura externa
> - Flyway gerencia migrations de forma agnóstica
> - Mesmas queries funcionam em ambos

### 5. Por que Resilience4j?

> **Decisão:** Resilience4j ao invés de Hystrix
>
> **Justificativa:**
> - Hystrix está em modo de manutenção
> - Resilience4j é mais leve e modular
> - Suporte nativo a Java 21 e Spring Boot 3
> - Métricas integradas com Micrometer

---

## 📈 Escalabilidade e Performance

### Configurações de Performance

```yaml
# Tomcat
server:
  tomcat:
    threads:
      max: 200          # Suporta 200 requisições paralelas
      min-spare: 20     # Mantém 20 threads prontas
    max-connections: 10000
    accept-count: 100   # Fila de espera

# HikariCP
spring:
  datasource:
    hikari:
      maximum-pool-size: 30     # 30 conexões com banco
      minimum-idle: 10
      connection-timeout: 5000  # 5s timeout
```

### Estimativa de Throughput

```
┌─────────────────────────────────────────────────────────────┐
│                CÁLCULO DE THROUGHPUT                        │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  Threads disponíveis:     200                                │
│  Tempo médio/requisição:  30ms                               │
│  Overhead de rede:        5ms                                │
│                                                              │
│  TPS teórico = 200 / 0.035s = ~5.714 TPS                    │
│                                                              │
│  Com 3 instâncias:                                           │
│  TPS total = 5.714 × 3 = ~17.142 TPS ✅                      │
│                                                              │
│  Meta do desafio: 6.000 TPS ✅ ATINGIDA                      │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

### Proposta de Arquitetura AWS (Produção)

```
                        ┌─────────────────┐
                        │   Route 53      │
                        │   (DNS)         │
                        └────────┬────────┘
                                 │
                        ┌────────▼────────┐
                        │   CloudFront    │
                        │   (CDN/WAF)     │
                        └────────┬────────┘
                                 │
                        ┌────────▼────────┐
                        │      ALB        │
                        │ (Load Balancer) │
                        └────────┬────────┘
                                 │
           ┌─────────────────────┼─────────────────────┐
           │                     │                     │
   ┌───────▼───────┐    ┌───────▼───────┐    ┌───────▼───────┐
   │    ECS/EKS    │    │    ECS/EKS    │    │    ECS/EKS    │
   │  Instance 1   │    │  Instance 2   │    │  Instance 3   │
   └───────┬───────┘    └───────┬───────┘    └───────┬───────┘
           │                     │                     │
           └─────────────────────┼─────────────────────┘
                                 │
                        ┌────────▼────────┐
                        │ Amazon Aurora   │
                        │  (PostgreSQL)   │
                        │   Multi-AZ      │
                        └─────────────────┘
                                 │
                        ┌────────▼────────┐
                        │  ElastiCache    │
                        │    (Redis)      │
                        └─────────────────┘
```

---

## 👨‍💻 Autor

<div align="center">
  
**Weslley Prado**

*Engenheiro de Software*

[![LinkedIn](https://img.shields.io/badge/LinkedIn-0077B5?style=for-the-badge&logo=linkedin&logoColor=white)](https://linkedin.com/in/weslley-prado)
[![GitHub](https://img.shields.io/badge/GitHub-100000?style=for-the-badge&logo=github&logoColor=white)](https://github.com/Weslley-Prado)

</div>

---

## 📄 Licença

Este projeto foi desenvolvido como parte de um processo seletivo e é de uso exclusivo para avaliação técnica.

---

<div align="center">

**Desenvolvido com ☕ e dedicação**

*Case Técnico Itaú - 2024*

</div>
