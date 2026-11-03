INSERT INTO clientes (id, nome, documento, email, telefone, rua, numero, complemento, bairro, cidade, cep, estado)
VALUES ('0192f0a0-0000-7000-8000-0000000000d1', 'Maria da Silva', '52998224725', 'maria@exemplo.com', '11999990000',
        'Rua das Flores', '123', NULL, 'Centro', 'São Paulo', '01001000', 'SP')
ON CONFLICT (id) DO NOTHING;

INSERT INTO veiculos (id, cliente_id, placa, marca, modelo, ano)
VALUES ('0192f0a0-0000-7000-8000-0000000000e1', '0192f0a0-0000-7000-8000-0000000000d1', 'ABC1D23', 'Fiat', 'Argo', 2022)
ON CONFLICT (id) DO NOTHING;

INSERT INTO ordens_servico (id, cliente_id, veiculo_id, relato_problema, status, data_abertura, codigo_acesso)
VALUES ('0192f0a0-0000-7000-8000-0000000000a1', '0192f0a0-0000-7000-8000-0000000000d1',
        '0192f0a0-0000-7000-8000-0000000000e1', 'Barulho na suspensão dianteira', 'AGUARDANDO_APROVACAO',
        CURRENT_TIMESTAMP - INTERVAL '2 hours', 'DEMO12')
    ON CONFLICT (id) DO NOTHING;

INSERT INTO historico_status_ordem_servico (id, ordem_servico_id, status_anterior, status_novo, data_transicao)
VALUES ('0192f0a0-0000-7000-8000-0000000000a1', '0192f0a0-0000-7000-8000-0000000000a1', NULL, 'RECEBIDA',
        CURRENT_TIMESTAMP - INTERVAL '2 hours'),
       ('0192f0a0-0000-7000-8000-0000000000a2', '0192f0a0-0000-7000-8000-0000000000a1', 'RECEBIDA',
        'EM_DIAGNOSTICO', CURRENT_TIMESTAMP - INTERVAL '90 minutes'),
       ('0192f0a0-0000-7000-8000-0000000000a3', '0192f0a0-0000-7000-8000-0000000000a1', 'EM_DIAGNOSTICO',
        'AGUARDANDO_APROVACAO', CURRENT_TIMESTAMP)
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

-- OS finalizadas (ciclo de vida completo) para demonstração de histórico
INSERT INTO ordens_servico (id, cliente_id, veiculo_id, relato_problema, status, data_abertura, data_conclusao, codigo_acesso)
VALUES ('0192f0a0-0000-7000-8000-0000000000f1', '0192f0a0-0000-7000-8000-0000000000d1',
        '0192f0a0-0000-7000-8000-0000000000e1', 'Vazamento no radiador', 'FINALIZADA',
        CURRENT_TIMESTAMP - INTERVAL '3 days', CURRENT_TIMESTAMP - INTERVAL '2 days 16 hours', 'DEMO34'),
       ('0192f0a0-0000-7000-8000-0000000000f2', '0192f0a0-0000-7000-8000-0000000000d1',
        '0192f0a0-0000-7000-8000-0000000000e1', 'Pastilhas de freio dianteiras desgastadas', 'FINALIZADA',
        CURRENT_TIMESTAMP - INTERVAL '7 days', CURRENT_TIMESTAMP - INTERVAL '6 days 16 hours', 'DEMO56')
ON CONFLICT (id) DO NOTHING;

INSERT INTO historico_status_ordem_servico (id, ordem_servico_id, status_anterior, status_novo, data_transicao)
VALUES ('0192f0a0-0000-7000-8000-000000000f12', '0192f0a0-0000-7000-8000-0000000000f1', NULL, 'RECEBIDA',
        CURRENT_TIMESTAMP - INTERVAL '3 days'),
       ('0192f0a0-0000-7000-8000-000000000f13', '0192f0a0-0000-7000-8000-0000000000f1', 'RECEBIDA', 'EM_DIAGNOSTICO',
        CURRENT_TIMESTAMP - INTERVAL '2 days 23 hours'),
       ('0192f0a0-0000-7000-8000-000000000f14', '0192f0a0-0000-7000-8000-0000000000f1', 'EM_DIAGNOSTICO', 'AGUARDANDO_APROVACAO',
        CURRENT_TIMESTAMP - INTERVAL '2 days 22 hours'),
       ('0192f0a0-0000-7000-8000-000000000f15', '0192f0a0-0000-7000-8000-0000000000f1', 'AGUARDANDO_APROVACAO', 'APROVADA',
        CURRENT_TIMESTAMP - INTERVAL '2 days 20 hours'),
       ('0192f0a0-0000-7000-8000-000000000f16', '0192f0a0-0000-7000-8000-0000000000f1', 'APROVADA', 'EM_EXECUCAO',
        CURRENT_TIMESTAMP - INTERVAL '2 days 18 hours'),
       ('0192f0a0-0000-7000-8000-000000000f17', '0192f0a0-0000-7000-8000-0000000000f1', 'EM_EXECUCAO', 'FINALIZADA',
        CURRENT_TIMESTAMP - INTERVAL '2 days 16 hours'),
       ('0192f0a0-0000-7000-8000-000000000f21', '0192f0a0-0000-7000-8000-0000000000f2', NULL, 'RECEBIDA',
        CURRENT_TIMESTAMP - INTERVAL '7 days'),
       ('0192f0a0-0000-7000-8000-000000000f22', '0192f0a0-0000-7000-8000-0000000000f2', 'RECEBIDA', 'EM_DIAGNOSTICO',
        CURRENT_TIMESTAMP - INTERVAL '6 days 23 hours'),
       ('0192f0a0-0000-7000-8000-000000000f23', '0192f0a0-0000-7000-8000-0000000000f2', 'EM_DIAGNOSTICO', 'AGUARDANDO_APROVACAO',
        CURRENT_TIMESTAMP - INTERVAL '6 days 22 hours'),
       ('0192f0a0-0000-7000-8000-000000000f24', '0192f0a0-0000-7000-8000-0000000000f2', 'AGUARDANDO_APROVACAO', 'APROVADA',
        CURRENT_TIMESTAMP - INTERVAL '6 days 20 hours'),
       ('0192f0a0-0000-7000-8000-000000000f25', '0192f0a0-0000-7000-8000-0000000000f2', 'APROVADA', 'EM_EXECUCAO',
        CURRENT_TIMESTAMP - INTERVAL '6 days 18 hours'),
       ('0192f0a0-0000-7000-8000-000000000f26', '0192f0a0-0000-7000-8000-0000000000f2', 'EM_EXECUCAO', 'FINALIZADA',
        CURRENT_TIMESTAMP - INTERVAL '6 days 16 hours')
ON CONFLICT (id) DO NOTHING;

INSERT INTO servicos (id, nome, descricao, valor_mao_de_obra)
VALUES ('0192f0a0-0000-7000-8000-0000000000b1', 'Troca de óleo', 'Troca do óleo do motor e do filtro de óleo', 200.00)
ON CONFLICT (id) DO NOTHING;

-- Peça do orçamento de demonstração, com as 4 unidades reservadas.
INSERT INTO produtos_estoque (id, nome_peca, quantidade_disponivel, quantidade_reservada, valor_unitario)
VALUES ('0192f0a0-0000-7000-8000-0000000000c1', 'ÓLEO 5W30 (LITRO)', 20, 4, 40.00)
ON CONFLICT (id) DO NOTHING;
