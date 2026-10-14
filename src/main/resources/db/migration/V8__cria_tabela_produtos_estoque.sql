CREATE TABLE produtos_estoque
(
    id                    UUID PRIMARY KEY,
    nome_peca             VARCHAR(255)   NOT NULL,
    quantidade_disponivel INT            NOT NULL,
    quantidade_reservada  INT            NOT NULL,
    valor_unitario        DECIMAL(19, 2) NOT NULL
);