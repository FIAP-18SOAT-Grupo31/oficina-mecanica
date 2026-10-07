CREATE TABLE orcamentos (
    id               UUID PRIMARY KEY,
    ordem_servico_id UUID           NOT NULL,
    valor_total      NUMERIC(38, 2) NOT NULL,
    status           VARCHAR(255)   NOT NULL,
    data_criacao     TIMESTAMP(6)   NOT NULL,
    data_validade    TIMESTAMP(6)   NOT NULL
);

CREATE INDEX idx_orcamentos_ordem_servico ON orcamentos (ordem_servico_id);

CREATE TABLE orcamento_itens_servico (
    id                  UUID PRIMARY KEY,
    orcamento_id        UUID           REFERENCES orcamentos (id),
    catalogo_servico_id UUID           NOT NULL,
    descricao           VARCHAR(255)   NOT NULL,
    valor_mao_de_obra   NUMERIC(38, 2) NOT NULL
);

CREATE TABLE orcamento_itens_peca (
    id                 UUID PRIMARY KEY,
    orcamento_id       UUID           REFERENCES orcamentos (id),
    produto_estoque_id UUID           NOT NULL,
    nome_peca          VARCHAR(255)   NOT NULL,
    quantidade         INTEGER        NOT NULL,
    valor_unitario     NUMERIC(38, 2) NOT NULL
);
