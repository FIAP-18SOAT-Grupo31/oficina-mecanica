CREATE TABLE servicos (
    id                UUID PRIMARY KEY,
    nome              VARCHAR(100)   NOT NULL,
    descricao         VARCHAR(255),
    valor_mao_de_obra NUMERIC(12, 2) NOT NULL,
    CONSTRAINT uk_servicos_nome UNIQUE (nome)
);
