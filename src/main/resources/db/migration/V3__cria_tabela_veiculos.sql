CREATE TABLE veiculos (
    id         UUID PRIMARY KEY,
    cliente_id UUID        NOT NULL REFERENCES clientes (id),
    placa      VARCHAR(7)  NOT NULL UNIQUE,
    marca      VARCHAR(50) NOT NULL,
    modelo     VARCHAR(50) NOT NULL,
    ano        INTEGER     NOT NULL
);

CREATE INDEX idx_veiculos_cliente ON veiculos (cliente_id);
