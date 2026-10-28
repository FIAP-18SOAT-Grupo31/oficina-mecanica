-- Catálogo de serviços da oficina. O orçamento guarda o id do serviço junto com uma cópia do nome e do valor
-- (orcamento_itens_servico), por isso não há chave estrangeira: mudar o catálogo não altera orçamentos antigos.
CREATE TABLE servicos (
    id                UUID PRIMARY KEY,
    nome              VARCHAR(100)   NOT NULL,
    descricao         VARCHAR(255),
    valor_mao_de_obra NUMERIC(12, 2) NOT NULL,
    CONSTRAINT uk_servicos_nome UNIQUE (nome)
);
