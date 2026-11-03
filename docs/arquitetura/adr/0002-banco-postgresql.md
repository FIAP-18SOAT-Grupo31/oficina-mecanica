# ADR 0002: PostgreSQL como banco de dados

- **Data:** decisão tomada no setup do projeto, em 25/09/2026; registro escrito em 28/10/2026
- **Status:** Aceita
- **Responsável:** Ruan Pablo e Henrique Melo

## Contexto

O enunciado deixa o banco livre, mas pede justificativa. Os dados são relacionais: cliente tem veículos, a OS liga cliente e veículo, o orçamento pertence à OS e tem itens de serviço e de peça. Algumas operações mexem em várias tabelas ao mesmo tempo, por exemplo aprovar um orçamento, que altera o orçamento, a OS e o saldo de várias peças.

## Decisão

Usamos PostgreSQL 16. O Flyway versiona o esquema com migrations em `src/main/resources/db/migration`, e a aplicação acessa o banco pelo Spring Data JPA.

Deixamos no banco as regras que ele consegue segurar: FK entre as tabelas e `UNIQUE` em documento do cliente, placa e nome da peça. Os ids são `UUID` e os valores monetários são `NUMERIC`.

## Alternativas consideradas

- MySQL: também atenderia. Ficamos com o PostgreSQL porque o grupo já conhecia melhor.
- MongoDB: o domínio é relacional e precisamos de transação envolvendo vários registros. Em documento isso dá mais trabalho e perde as FKs.
- H2 em memória como banco da aplicação: os dados precisam continuar salvos entre reinicializações, então o H2 ficou só nos testes.

## Consequências

Positivas:

- Transação ACID para a aprovação do orçamento e para a reserva de peças.
- O banco sobe pelo `docker-compose.yml` (imagem oficial `postgres:16-alpine`), junto com o pgAdmin.

Negativas:

- Os testes de integração rodam em H2 no modo PostgreSQL, e só o `PostgresRealIntegrationTest` usa um PostgreSQL real (Testcontainers, pulado quando não há Docker).
- Risco que aceitamos: o H2 não se comporta igual ao PostgreSQL em tudo, então uma diferença de SQL pode passar nos testes e só aparecer no banco real.
