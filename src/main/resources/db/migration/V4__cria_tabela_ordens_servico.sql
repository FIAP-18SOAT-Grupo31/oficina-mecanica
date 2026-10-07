CREATE TABLE ordens_servico (
    id              UUID PRIMARY KEY,
    cliente_id      UUID         NOT NULL REFERENCES clientes (id),
    veiculo_id      UUID         NOT NULL REFERENCES veiculos (id),
    relato_problema VARCHAR(500) NOT NULL,
    status          VARCHAR(30)  NOT NULL,
    data_abertura   TIMESTAMP(6) NOT NULL,
    data_conclusao  TIMESTAMP(6)
);

CREATE INDEX idx_ordens_servico_cliente ON ordens_servico (cliente_id);
CREATE INDEX idx_ordens_servico_veiculo ON ordens_servico (veiculo_id);

ALTER TABLE orcamentos
    ADD CONSTRAINT fk_orcamentos_ordem_servico FOREIGN KEY (ordem_servico_id) REFERENCES ordens_servico (id);
