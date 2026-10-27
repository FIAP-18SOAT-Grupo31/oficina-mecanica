-- Clientes que ja existiam antes desta migration nao tem endereco. Por isso as
-- colunas obrigatorias entram sem NOT NULL, recebem um valor provisorio e so
-- depois passam a ser NOT NULL (um ADD COLUMN ... NOT NULL direto falharia em
-- bancos que ja tem clientes). O mesmo padrao foi usado na V7.
ALTER TABLE clientes ADD COLUMN rua VARCHAR(150);
ALTER TABLE clientes ADD COLUMN numero VARCHAR(10);
ALTER TABLE clientes ADD COLUMN complemento VARCHAR(100);
ALTER TABLE clientes ADD COLUMN bairro VARCHAR(100);
ALTER TABLE clientes ADD COLUMN cidade VARCHAR(100);
ALTER TABLE clientes ADD COLUMN cep VARCHAR(9);
ALTER TABLE clientes ADD COLUMN estado VARCHAR(30);

-- Valores provisorios, validos para o dominio (Endereco). O estado precisa ser
-- uma sigla valida; 'SP' e apenas um placeholder e deve ser corrigido no cadastro.
UPDATE clientes
SET rua    = 'Não informado',
    numero = 'S/N',
    bairro = 'Não informado',
    cidade = 'Não informado',
    cep    = '00000000',
    estado = 'SP';

ALTER TABLE clientes ALTER COLUMN rua SET NOT NULL;
ALTER TABLE clientes ALTER COLUMN numero SET NOT NULL;
ALTER TABLE clientes ALTER COLUMN bairro SET NOT NULL;
ALTER TABLE clientes ALTER COLUMN cidade SET NOT NULL;
ALTER TABLE clientes ALTER COLUMN cep SET NOT NULL;
ALTER TABLE clientes ALTER COLUMN estado SET NOT NULL;
