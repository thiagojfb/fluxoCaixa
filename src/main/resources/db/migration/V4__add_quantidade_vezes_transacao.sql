ALTER TABLE transacao
    ADD COLUMN quantidade_vezes INTEGER NOT NULL DEFAULT 1;

ALTER TABLE transacao
    ADD CONSTRAINT chk_transacao_quantidade_vezes_positiva CHECK (quantidade_vezes > 0);

ALTER TABLE historico_transacao
    ADD COLUMN quantidade_vezes INTEGER NOT NULL DEFAULT 1;

ALTER TABLE historico_transacao
    ADD CONSTRAINT chk_historico_quantidade_vezes_nao_negativa CHECK (quantidade_vezes >= 0);
