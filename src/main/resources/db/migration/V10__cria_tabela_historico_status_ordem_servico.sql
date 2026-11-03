CREATE TABLE historico_status_ordem_servico (
    id                UUID PRIMARY KEY,
    ordem_servico_id  UUID         NOT NULL REFERENCES ordens_servico (id),
    status_anterior   VARCHAR(30),
    status_novo       VARCHAR(30)  NOT NULL,
    data_transicao    TIMESTAMP(6) NOT NULL
);

CREATE INDEX idx_historico_status_os ON historico_status_ordem_servico (ordem_servico_id, data_transicao);
