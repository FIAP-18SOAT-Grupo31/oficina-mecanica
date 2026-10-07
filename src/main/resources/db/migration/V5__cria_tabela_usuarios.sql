CREATE TABLE usuarios (
    id         UUID PRIMARY KEY,
    login      VARCHAR(60)  NOT NULL UNIQUE,
    senha_hash VARCHAR(100) NOT NULL,
    papel      VARCHAR(20)  NOT NULL
);
