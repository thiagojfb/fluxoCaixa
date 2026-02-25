CREATE TABLE budget (
    id          UUID            PRIMARY KEY,
    user_id     VARCHAR(255)    NOT NULL,
    salario     DECIMAL(19,2)   NOT NULL DEFAULT 0,
    criado_em   TIMESTAMP       NOT NULL,
    atualizado_em TIMESTAMP     NOT NULL,
    version     BIGINT          NOT NULL DEFAULT 0,
    CONSTRAINT uk_budget_user UNIQUE (user_id)
);

CREATE TABLE transaction (
    id          UUID            PRIMARY KEY,
    user_id     VARCHAR(255)    NOT NULL,
    tipo        VARCHAR(20)     NOT NULL,
    descricao   VARCHAR(500),
    valor       DECIMAL(19,2)   NOT NULL,
    data_hora   TIMESTAMP       NOT NULL,
    criado_em   TIMESTAMP       NOT NULL,
    CONSTRAINT chk_tipo CHECK (tipo IN ('CREDIT', 'DEBIT_PIX')),
    CONSTRAINT chk_valor_positivo CHECK (valor > 0)
);

CREATE INDEX idx_transaction_user_id ON transaction (user_id);
CREATE INDEX idx_transaction_data_hora ON transaction (data_hora);
CREATE INDEX idx_transaction_tipo ON transaction (tipo);
