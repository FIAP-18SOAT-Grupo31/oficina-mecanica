ALTER TABLE ordens_servico ADD COLUMN codigo_acesso VARCHAR(6);

UPDATE ordens_servico SET codigo_acesso = 'DEMO12' WHERE codigo_acesso IS NULL;

ALTER TABLE ordens_servico ALTER COLUMN codigo_acesso SET NOT NULL;