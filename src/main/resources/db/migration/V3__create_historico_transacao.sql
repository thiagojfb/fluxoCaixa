CREATE TABLE historico_transacao (
    id          UUID            PRIMARY KEY,
    user_id     VARCHAR(255)    NOT NULL,
    tipo        VARCHAR(20)     NOT NULL,
    descricao   VARCHAR(500),
    valor       DECIMAL(19,2)   NOT NULL,
    data_hora   TIMESTAMP       NOT NULL,
    criado_em   TIMESTAMP       NOT NULL,
    fechado_em  TIMESTAMP       NOT NULL,
    CONSTRAINT chk_historico_tipo CHECK (tipo IN ('CREDIT', 'DEBIT_PIX')),
    CONSTRAINT chk_historico_valor_positivo CHECK (valor > 0)
);

CREATE INDEX idx_historico_usuario_id ON historico_transacao (user_id);
CREATE INDEX idx_historico_fechado_em ON historico_transacao (fechado_em);
