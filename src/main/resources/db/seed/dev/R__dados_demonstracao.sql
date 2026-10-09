INSERT INTO clientes (id, nome, documento, email, telefone)
VALUES ('0192f0a0-0000-7000-8000-0000000000d1', 'Maria da Silva', '52998224725', 'maria@exemplo.com', '11999990000')
ON CONFLICT (id) DO NOTHING;

INSERT INTO veiculos (id, cliente_id, placa, marca, modelo, ano)
VALUES ('0192f0a0-0000-7000-8000-0000000000e1', '0192f0a0-0000-7000-8000-0000000000d1', 'ABC1D23', 'Fiat', 'Argo', 2022)
ON CONFLICT (id) DO NOTHING;

INSERT INTO ordens_servico (id, cliente_id, veiculo_id, relato_problema, status, data_abertura, codigo_acesso)
VALUES ('0192f0a0-0000-7000-8000-0000000000a1', '0192f0a0-0000-7000-8000-0000000000d1',
        '0192f0a0-0000-7000-8000-0000000000e1', 'Barulho na suspensão dianteira', 'AGUARDANDO_APROVACAO',
        CURRENT_TIMESTAMP, 'DEMO12')
    ON CONFLICT (id) DO NOTHING;

INSERT INTO orcamentos (id, ordem_servico_id, valor_total, status, data_criacao, data_validade)
VALUES ('0192f0a0-0000-7000-8000-000000000001', '0192f0a0-0000-7000-8000-0000000000a1', 360.00, 'PENDENTE',
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '7 days')
ON CONFLICT (id) DO NOTHING;

INSERT INTO orcamento_itens_servico (id, orcamento_id, catalogo_servico_id, descricao, valor_mao_de_obra)
VALUES ('0192f0a0-0000-7000-8000-000000000011', '0192f0a0-0000-7000-8000-000000000001',
        '0192f0a0-0000-7000-8000-0000000000b1', 'Troca de óleo', 200.00)
ON CONFLICT (id) DO NOTHING;

INSERT INTO orcamento_itens_peca (id, orcamento_id, produto_estoque_id, nome_peca, quantidade, valor_unitario)
VALUES ('0192f0a0-0000-7000-8000-000000000021', '0192f0a0-0000-7000-8000-000000000001',
        '0192f0a0-0000-7000-8000-0000000000c1', 'Óleo 5W30 (litro)', 4, 40.00)
ON CONFLICT (id) DO NOTHING;
