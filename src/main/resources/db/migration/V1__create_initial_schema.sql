-- ============================================
-- Itaú Transfer API - Database Schema
-- Version: 1.0.0
-- ============================================

-- Tabela de Contas Correntes
CREATE TABLE accounts (
    id UUID PRIMARY KEY,
    account_number VARCHAR(10) NOT NULL,
    agency_number VARCHAR(6) NOT NULL,
    client_id UUID NOT NULL,
    balance DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    available_limit DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    
    CONSTRAINT uk_account_agency UNIQUE (account_number, agency_number),
    CONSTRAINT chk_balance_positive CHECK (balance >= 0),
    CONSTRAINT chk_limit_positive CHECK (available_limit >= 0),
    CONSTRAINT chk_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'BLOCKED', 'CLOSED'))
);

-- Índices para contas
CREATE INDEX idx_accounts_client_id ON accounts(client_id);
CREATE INDEX idx_accounts_status ON accounts(status);

-- Tabela de Transferências
CREATE TABLE transfers (
    id UUID PRIMARY KEY,
    source_account_id UUID NOT NULL,
    target_account_id UUID NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    failure_reason VARCHAR(500),
    bacen_notification_id VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    bacen_notified_at TIMESTAMP,
    bacen_retry_count INT NOT NULL DEFAULT 0,
    version BIGINT NOT NULL DEFAULT 0,
    
    CONSTRAINT fk_transfer_source_account FOREIGN KEY (source_account_id) REFERENCES accounts(id),
    CONSTRAINT fk_transfer_target_account FOREIGN KEY (target_account_id) REFERENCES accounts(id),
    CONSTRAINT chk_amount_positive CHECK (amount > 0),
    CONSTRAINT chk_transfer_status CHECK (status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED', 'BACEN_PENDING', 'BACEN_NOTIFIED')),
    CONSTRAINT chk_different_accounts CHECK (source_account_id != target_account_id)
);

-- Índices para transferências
CREATE INDEX idx_transfers_source_account ON transfers(source_account_id);
CREATE INDEX idx_transfers_target_account ON transfers(target_account_id);
CREATE INDEX idx_transfers_status ON transfers(status);
CREATE INDEX idx_transfers_created_at ON transfers(created_at);
CREATE INDEX idx_transfers_bacen_pending ON transfers(status, bacen_retry_count) WHERE status = 'BACEN_PENDING';

-- Tabela de Limite Diário de Transferência
CREATE TABLE daily_transfer_limits (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id UUID NOT NULL,
    date DATE NOT NULL,
    used_amount DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    daily_limit DECIMAL(15,2) NOT NULL DEFAULT 1000.00,
    version BIGINT NOT NULL DEFAULT 0,
    
    CONSTRAINT uk_account_date UNIQUE (account_id, date),
    CONSTRAINT fk_daily_limit_account FOREIGN KEY (account_id) REFERENCES accounts(id),
    CONSTRAINT chk_used_amount_positive CHECK (used_amount >= 0),
    CONSTRAINT chk_daily_limit_positive CHECK (daily_limit > 0)
);

-- Índice para limite diário
CREATE INDEX idx_daily_limits_account_date ON daily_transfer_limits(account_id, date);

-- Comentários nas tabelas
COMMENT ON TABLE accounts IS 'Contas correntes dos clientes';
COMMENT ON TABLE transfers IS 'Histórico de transferências realizadas';
COMMENT ON TABLE daily_transfer_limits IS 'Controle de limite diário de transferência por conta';

-- Comentários nas colunas principais
COMMENT ON COLUMN accounts.status IS 'Status da conta: ACTIVE, INACTIVE, BLOCKED, CLOSED';
COMMENT ON COLUMN transfers.status IS 'Status da transferência: PENDING, PROCESSING, COMPLETED, FAILED, BACEN_PENDING, BACEN_NOTIFIED';
COMMENT ON COLUMN transfers.bacen_retry_count IS 'Número de tentativas de notificação ao BACEN';
COMMENT ON COLUMN daily_transfer_limits.daily_limit IS 'Limite diário de transferência em reais (padrão: R$ 1.000,00)';


