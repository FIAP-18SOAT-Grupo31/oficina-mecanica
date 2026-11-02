ALTER TABLE produtos_estoque
    ADD CONSTRAINT uk_produtos_estoque_nome_peca UNIQUE (nome_peca);