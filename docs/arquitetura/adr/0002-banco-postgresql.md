# ADR 0002: PostgreSQL como banco de dados

Status: aceito (25/09/2026, commit `0890f41`; migrations Flyway desde `85b0b08`, 07/10/2026)

## Contexto

O enunciado deixa o banco livre, mas pede justificativa. Os dados são relacionais: cliente tem veículos, a OS liga cliente e veículo, o orçamento pertence à OS e tem itens de serviço e de peça. Algumas operações mexem em várias tabelas ao mesmo tempo, por exemplo aprovar um orçamento, que altera o orçamento, a OS e o saldo de várias peças.

## Decisão

Usar PostgreSQL 16, com o esquema versionado por migrations Flyway em `src/main/resources/db/migration` e acesso via Spring Data JPA. As regras que o banco consegue segurar ficam nele também: FK entre as tabelas e `UNIQUE` em documento do cliente, placa e nome da peça. Ids são `UUID` e valores são `NUMERIC`.

## Alternativas consideradas

- MySQL: também atenderia. Ficamos com o PostgreSQL porque o grupo já conhecia melhor.
- MongoDB: o domínio é relacional e precisamos de transação envolvendo vários registros. Em documento isso dá mais trabalho e perde as FKs.
- H2 em memória como banco da aplicação: serve para teste, não para rodar de verdade.

## Consequências

- Transação ACID para a aprovação do orçamento e para a reserva de peças.
- O banco sobe pelo `docker-compose.yml` (imagem oficial `postgres:16-alpine`), junto com o pgAdmin.
- Os testes de integração rodam em H2 no modo PostgreSQL, e só o `PostgresRealIntegrationTest` usa um PostgreSQL real (Testcontainers, pulado sem Docker).
- Débito técnico: o H2 não se comporta igual ao PostgreSQL em tudo, então uma diferença de SQL pode passar nos testes e só aparecer no banco real.
