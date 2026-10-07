CREATE TABLE clientes (
    id        UUID PRIMARY KEY,
    nome      VARCHAR(150) NOT NULL,
    documento VARCHAR(14)  NOT NULL UNIQUE,
    email     VARCHAR(150),
    telefone  VARCHAR(20)
);
