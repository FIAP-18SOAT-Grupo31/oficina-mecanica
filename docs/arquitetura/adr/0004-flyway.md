# ADR 0004: Flyway para migrations e dados de demonstração

Data: 06/10/2026
Status: aceita

## Contexto

Com `ddl-auto=update`, o Hibernate altera o banco sozinho e sem histórico. Em produção isso é arriscado, e cada pessoa do grupo acabaria com um schema diferente.

## Decisão

Flyway, com scripts SQL em `db/migration`. Dados de demonstração ficam em `db/seed/dev` e só rodam no profile `dev`. O Hibernate apenas valida o schema.

## Alternativas

- Liquibase: mais recursos, mas usa XML ou YAML e é mais verboso para o tamanho do projeto.
- `ddl-auto=update`: sem histórico e sem revisão.

## Consequências

Toda mudança de tabela vira um arquivo novo (`V3__...`) revisado no PR. Migration que já foi para a `dev` não se altera.
