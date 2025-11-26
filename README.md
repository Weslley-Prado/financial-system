# Itaú Transfer API

> Case Técnico Itaú - API de Transferências Bancárias de Alta Performance

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.1-green)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue)
![Docker](https://img.shields.io/badge/Docker-Ready-blue)

## 📋 Descrição

Sistema de transferências bancárias desenvolvido como case técnico do Itaú. A API permite realizar **consulta de saldo** e **transferências entre contas correntes** com alta disponibilidade, resiliência e performance.

### Requisitos Atendidos

- ✅ Buscar nome do cliente na API de Cadastro (Mock)
- ✅ Validar se a conta corrente está ativa
- ✅ Validar limite disponível na Conta Corrente
- ✅ Validar limite diário de R$ 1.000,00
- ✅ Notificar BACEN de forma síncrona
- ✅ Tratamento de rate limit (HTTP 429) do BACEN
- ✅ Padrões de resiliência (Circuit Breaker, Retry, Rate Limiter)
- ✅ Testes unitários e automatizados
- ✅ Design patterns e Clean Architecture

## 🏗️ Arquitetura

```
┌─────────────────────────────────────────────────────────────────┐
│                        Transfer API                              │
├─────────────────────────────────────────────────────────────────┤
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────────┐  │
│  │    Web      │  │ Application │  │        Domain           │  │
│  │  (REST API) │→ │  (Use Cases)│→ │  (Entities, Rules)      │  │
│  └─────────────┘  └─────────────┘  └─────────────────────────┘  │
│         ↓                ↓                      ↓                │
│  ┌─────────────────────────────────────────────────────────────┐│
│  │                    Infrastructure                            ││
│  │  ┌───────────┐ ┌─────────────┐ ┌────────────────────────┐   ││
│  │  │PostgreSQL │ │ Cadastro API│ │      BACEN API         │   ││
│  │  │   (JPA)   │ │   (Client)  │ │  (Client + Resilience) │   ││
│  │  └───────────┘ └─────────────┘ └────────────────────────┘   ││
│  └─────────────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────────────┘
```

### Padrões Utilizados

- **Clean Architecture / Hexagonal Architecture**: Separação clara de responsabilidades
- **Domain-Driven Design (DDD)**: Entidades ricas, Value Objects, Aggregates
- **Ports & Adapters**: Inversão de dependência para integrações
- **Circuit Breaker**: Proteção contra falhas em cascata
- **Retry Pattern**: Resiliência para falhas temporárias
- **Rate Limiter**: Controle de taxa de requisições
- **Bulkhead**: Isolamento de recursos

## 🚀 Tecnologias

| Tecnologia | Versão | Propósito |
|------------|--------|-----------|
| Java | 21 | Linguagem principal |
| Spring Boot | 3.2.1 | Framework web |
| Spring Data JPA | - | Persistência |
| Resilience4j | 2.2.0 | Padrões de resiliência |
| PostgreSQL | 16 | Banco de dados |
| Caffeine | - | Cache de alta performance |
| Flyway | - | Migrações de banco |
| OpenAPI/Swagger | 3.0 | Documentação da API |
| Micrometer + Prometheus | - | Métricas |
| Docker | - | Containerização |

## 📦 Estrutura do Projeto

```
src/main/java/com/itau/transferapi/
├── domain/                     # Camada de Domínio
│   ├── entity/                 # Entidades de domínio
│   ├── valueobject/            # Value Objects imutáveis
│   ├── exception/              # Exceções de domínio
│   └── repository/             # Interfaces de repositório
├── application/                # Camada de Aplicação
│   ├── dto/                    # Data Transfer Objects
│   ├── port/                   # Portas (input/output)
│   └── usecase/                # Casos de uso
├── infrastructure/             # Camada de Infraestrutura
│   ├── adapter/                # Adaptadores (persistence, clients)
│   ├── config/                 # Configurações
│   ├── entity/                 # Entidades JPA
│   └── mock/                   # Mocks para desenvolvimento
└── web/                        # Camada Web
    ├── controller/             # Controllers REST
    └── exception/              # Handler de exceções
```

## ⚡ Quick Start

### Pré-requisitos

- Java 21+
- Maven 3.9+
- Docker & Docker Compose (opcional)

### Executar Localmente

```bash
# Clonar repositório
git clone <repository-url>
cd transfer-api

# Executar com perfil local (H2 in-memory)
./mvnw spring-boot:run -Dspring.profiles.active=local
```

### Executar com Docker

```bash
# Subir todos os serviços
docker-compose up -d

# Verificar logs
docker-compose logs -f transfer-api

# Parar serviços
docker-compose down
```

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
  "amount": 150.00,
  "description": "Pagamento"
}
```

### Consulta de Saldo

```http
GET /api/v1/accounts/{accountNumber}/balance?agencyNumber={agencyNumber}
```

### Documentação

- **Swagger UI**: http://localhost:8881/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8881/api-docs

### Monitoramento

- **Health Check**: http://localhost:8881/actuator/health
- **Métricas**: http://localhost:8881/actuator/prometheus
- **Circuit Breakers**: http://localhost:8881/actuator/circuitbreakers

## 🧪 Testes

```bash
# Executar todos os testes
./mvnw test

# Executar testes com cobertura
./mvnw test jacoco:report

# Relatório de cobertura
open target/site/jacoco/index.html
```

### Tipos de Testes

- **Unitários**: Domínio, Use Cases, Value Objects
- **Integração**: API REST, Persistência
- **Arquitetura**: Regras de dependência (ArchUnit)

## 📊 Observabilidade

### Métricas Disponíveis

| Métrica | Descrição |
|---------|-----------|
| `transfer.execution.time` | Tempo de execução de transferências |
| `balance.query.time` | Tempo de consulta de saldo |
| `resilience4j.circuitbreaker.*` | Estado dos circuit breakers |
| `resilience4j.ratelimiter.*` | Estado dos rate limiters |

### Dashboards (Grafana)

Após iniciar com Docker Compose:
- URL: http://localhost:3000
- Usuário: admin
- Senha: admin

## 🛡️ Padrões de Resiliência

### Circuit Breaker

```yaml
Cadastro API:
  - Sliding Window: 50 chamadas
  - Failure Rate Threshold: 40%
  - Wait Duration: 30s

BACEN API:
  - Sliding Window: 100 chamadas
  - Failure Rate Threshold: 50%
  - Wait Duration: 60s
```

### Retry

```yaml
Cadastro API:
  - Max Attempts: 2
  - Wait Duration: 300ms
  - Backoff: Exponencial

BACEN API:
  - Max Attempts: 5
  - Wait Duration: 1s
  - Backoff: Exponencial
```

### Rate Limiter (BACEN)

```yaml
- Limit: 100 requisições/segundo
- Timeout: 500ms
```

## 🔒 Segurança

- Validação de entrada com Bean Validation
- Tratamento centralizado de exceções
- Logs estruturados sem dados sensíveis
- Container executando como usuário não-root
- Health checks configurados

## 📈 Performance

A API foi projetada para suportar:
- **6.000 TPS** (transações por segundo)
- **Latência < 100ms** (P99)

Otimizações implementadas:
- Connection pooling (HikariCP)
- Cache com Caffeine
- Queries otimizadas com índices
- Compressão de resposta

## 🤝 Contribuição

1. Fork o projeto
2. Crie sua feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

## 📝 Licença

Este projeto foi desenvolvido como case técnico para o processo seletivo do Itaú.

---

**Desenvolvido com ❤️ para o Case Técnico Itaú**


