# 🏦 Itaú Transfer API - Postman Collection

## 📋 Descrição

Collection profissional do **Postman** para testes da API de Transferências do Itaú. Inclui todos os cenários de teste, documentação e testes automatizados.

## 📁 Arquivos

| Arquivo | Descrição |
|---------|-----------|
| `Itau_Transfer_API.postman_collection.json` | Collection principal com todos os endpoints |
| `Itau_Transfer_API.postman_environment.json` | Ambiente para desenvolvimento local |
| `Itau_Transfer_API_Docker.postman_environment.json` | Ambiente para execução em Docker |

## 🚀 Como Usar

### Importar no Postman

1. Abra o **Postman**
2. Clique em **Import** (ou `Ctrl+O`)
3. Selecione os arquivos:
   - `Itau_Transfer_API.postman_collection.json`
   - `Itau_Transfer_API.postman_environment.json`
4. Selecione o ambiente **"🏦 Itaú - Local Development"** no canto superior direito

### Executar via Newman (CLI)

```bash
# Instalar Newman
npm install -g newman newman-reporter-htmlextra

# Executar todos os testes
newman run Itau_Transfer_API.postman_collection.json \
  -e Itau_Transfer_API.postman_environment.json \
  --reporters cli,htmlextra \
  --reporter-htmlextra-export reports/test-report.html

# Executar pasta específica
newman run Itau_Transfer_API.postman_collection.json \
  -e Itau_Transfer_API.postman_environment.json \
  --folder "💸 Transferências"

# Executar com múltiplas iterações (teste de carga)
newman run Itau_Transfer_API.postman_collection.json \
  -e Itau_Transfer_API.postman_environment.json \
  --folder "🔄 Testes de Performance" \
  --iteration-count 100 \
  --delay-request 10
```

## 📂 Estrutura da Collection

```
🏦 Itaú Transfer API
├── 🏥 Health & Monitoring
│   ├── Health Check
│   ├── Métricas Prometheus
│   ├── Circuit Breakers Status
│   └── Rate Limiters Status
│
├── 💰 Consulta de Saldo
│   ├── Consultar Saldo - João (Conta Ativa)
│   ├── Consultar Saldo - Maria
│   ├── ❌ Conta Não Encontrada
│   └── ❌ Conta Inativa
│
├── 💸 Transferências
│   ├── ✅ Cenários de Sucesso
│   │   ├── Transferir R$ 100,00 (João → Maria)
│   │   ├── Transferir R$ 0,01 (Valor Mínimo)
│   │   └── Transferir R$ 500,00 (Valor Médio)
│   │
│   ├── ❌ Erros de Negócio
│   │   ├── Mesma Conta (ITAU-1004)
│   │   ├── Conta Origem Inexistente (ITAU-3001)
│   │   ├── Conta Origem Inativa (ITAU-2001)
│   │   ├── Saldo Insuficiente (ITAU-2002)
│   │   ├── Limite Insuficiente (ITAU-2003)
│   │   └── Limite Diário Excedido (ITAU-2004)
│   │
│   └── ⚠️ Erros de Validação
│       ├── Valor Negativo
│       ├── Valor Zero
│       ├── Campos Obrigatórios Vazios
│       └── Valor Acima do Máximo
│
├── 🔄 Testes de Performance
│   ├── Transferência com Valor Aleatório
│   └── Consulta Saldo (Teste de Cache)
│
├── 🔧 Mock APIs
│   ├── Mock Cadastro - Buscar João
│   ├── Mock Cadastro - Buscar Maria
│   └── Mock BACEN - Estatísticas
│
└── 📚 Documentação
    ├── Swagger UI
    └── OpenAPI Specification (JSON)
```

## 🧪 Contas de Teste

| Conta | Agência | Cliente | Saldo | Limite | Status |
|-------|---------|---------|-------|--------|--------|
| `12345-6` | `0001` | João Silva | R$ 5.000 | R$ 10.000 | ✅ Ativa |
| `98765-4` | `0002` | Maria Santos | R$ 3.000 | R$ 5.000 | ✅ Ativa |
| `11111-1` | `0001` | Carlos Oliveira | R$ 1.000 | R$ 2.000 | ❌ Inativa |
| `22222-2` | `0001` | João Silva | R$ 100 | R$ 50 | ✅ Ativa |

## 📊 Códigos de Erro

### Erros de Validação (HTTP 400)
| Código | Descrição |
|--------|-----------|
| `ITAU-1001` | Requisição inválida (campos obrigatórios, formato) |

### Erros de Negócio (HTTP 422)
| Código | Descrição |
|--------|-----------|
| `ITAU-1004` | Não é permitido transferir para a mesma conta |
| `ITAU-2001` | Conta não está ativa |
| `ITAU-2002` | Saldo insuficiente |
| `ITAU-2003` | Limite disponível insuficiente |
| `ITAU-2004` | Limite diário de transferência excedido |
| `ITAU-2005` | Cliente não está ativo |

### Erros de Recurso (HTTP 404)
| Código | Descrição |
|--------|-----------|
| `ITAU-3001` | Conta não encontrada |
| `ITAU-3002` | Cliente não encontrado |

### Erros de Integração (HTTP 429/503)
| Código | Descrição |
|--------|-----------|
| `ITAU-4005` | Rate limit do BACEN atingido |
| `ITAU-4003` | BACEN indisponível |

## ✅ Testes Automatizados

Cada requisição inclui testes automatizados para validar:

- **Status Code**: Código HTTP esperado
- **Schema**: Estrutura da resposta JSON
- **Business Rules**: Regras de negócio
- **Performance**: Tempo de resposta < 100ms
- **Error Codes**: Códigos de erro específicos

### Exemplo de Teste

```javascript
pm.test('Status code é 201 Created', function() {
    pm.response.to.have.status(201);
});

pm.test('Resposta contém transferId UUID', function() {
    const json = pm.response.json();
    pm.expect(json.transferId).to.match(/^[0-9a-f-]{36}$/);
});

pm.test('Performance: Resposta em menos de 100ms', function() {
    pm.expect(pm.response.responseTime).to.be.below(100);
});
```

## 🔗 URLs da Aplicação

| Serviço | URL |
|---------|-----|
| API Base | http://localhost:8881 |
| Swagger UI | http://localhost:8881/swagger-ui.html |
| Health Check | http://localhost:8881/actuator/health |
| Prometheus Metrics | http://localhost:8881/actuator/prometheus |
| H2 Console | http://localhost:8881/h2-console |

## 📈 Teste de Carga

Para executar testes de carga, use o **Collection Runner** ou **Newman**:

### Via Collection Runner (Postman UI)

1. Clique com botão direito na pasta **"🔄 Testes de Performance"**
2. Selecione **"Run folder"**
3. Configure:
   - **Iterations**: 100
   - **Delay**: 10ms
4. Clique em **"Run"**

### Via Newman (CLI)

```bash
newman run Itau_Transfer_API.postman_collection.json \
  -e Itau_Transfer_API.postman_environment.json \
  --folder "🔄 Testes de Performance" \
  --iteration-count 1000 \
  --delay-request 5
```

## 🛠️ Variáveis de Ambiente

| Variável | Descrição | Valor Padrão |
|----------|-----------|--------------|
| `baseUrl` | URL base da API | `http://localhost:8881` |
| `apiVersion` | Versão da API | `v1` |
| `contaJoao` | Conta do João | `12345-6` |
| `agenciaJoao` | Agência do João | `0001` |
| `contaMaria` | Conta da Maria | `98765-4` |
| `agenciaMaria` | Agência da Maria | `0002` |
| `contaInativa` | Conta inativa | `11111-1` |
| `contaLimiteBaixo` | Conta limite baixo | `22222-2` |

## 📝 Notas

- O mock do BACEN simula **10% de rate limit** (HTTP 429) para testes de resiliência
- O cache de clientes expira em **5 minutos**
- O limite diário de transferência é **R$ 1.000,00** por conta

---

**Desenvolvido para Case Técnico Itaú** 🏦
