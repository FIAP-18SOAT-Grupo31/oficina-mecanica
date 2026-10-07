# ADR 0002: PostgreSQL como banco de dados

Data: 06/10/2026
Status: aceita

## Contexto

Clientes, veículos, ordens de serviço, orçamentos, serviços e peças se relacionam o tempo todo. A aprovação do orçamento muda o status da ordem e baixa o estoque, e as duas coisas precisam acontecer juntas.

## Decisão

PostgreSQL 16, rodando no Docker pelo `docker-compose.yml`.

## Alternativas

- MongoDB: o modelo é cheio de relacionamentos, e a consistência entre documentos teria de ser garantida no código.
- MySQL: atenderia, mas não traz vantagem sobre o PostgreSQL neste caso.

## Consequências

O banco desfaz tudo quando um passo do fluxo falha, e as chaves estrangeiras e restrições de unicidade barram dados inconsistentes. Os testes de integração usam o mesmo banco via Testcontainers. O schema precisa de migrations versionadas (ADR 0004).
