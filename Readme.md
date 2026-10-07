# Sistema Integrado de Oficina Mecânica

Back-end do Tech Challenge da Fase 1 da pós-graduação SOAT da FIAP, feito pelo Grupo 31.

Uma oficina mecânica de médio porte controla atendimento, diagnóstico, orçamento e execução em planilhas e anotações. Este projeto é o MVP do back-end que organiza esse fluxo: identificação do cliente e do veículo, abertura da ordem de serviço, orçamento com serviços e peças, aprovação e acompanhamento do status.

## Stack

- Java 21 e Spring Boot 4
- PostgreSQL 16, com schema versionado pelo Flyway
- Spring Security com JWT
- Docker e Docker Compose
- JUnit 5, MockMvc, ArchUnit, H2 e Testcontainers nos testes
- GitHub Actions, Trivy e CodeQL para qualidade e segurança

## Como rodar

Pré-requisitos: Git, Docker e make. No Windows, use o WSL (Ubuntu) com o Docker Desktop ligado. Java 21 só é preciso para rodar fora do Docker.

```bash
git clone https://github.com/FIAP-18SOAT-Grupo31/oficina-mecanica.git
cd oficina-mecanica
make env      # cria o .env a partir do .env.example
make up       # compila e sobe Postgres, pgAdmin e a API
make smoke    # confere se tudo respondeu certo
```

| Serviço | Endereço |
|---|---|
| API | http://localhost:8080 |
| Swagger | http://localhost:8080/swagger-ui.html |
| pgAdmin | http://localhost:5050 |

O `.env` não vai para o git. No profile `dev` o banco sobe com dados de exemplo: um cliente, um veículo, uma ordem de serviço e um orçamento.

### Comandos

| Comando | O que faz |
|---|---|
| `make` | lista todos os comandos |
| `make up` | compila e sobe banco, pgAdmin e API em containers |
| `make down` | para e remove os containers; os dados do banco ficam |
| `make logs` | acompanha os logs da API |
| `make smoke` | confere health, Swagger, login e permissões |
| `make run` | roda a API fora do container, para debugar na IDE; o Postgres sobe sozinho |
| `make test` | roda os testes |
| `make verify` | testes e cobertura mínima de 80%, o mesmo que o CI roda |
| `make cobertura` | gera o relatório em `target/site/jacoco/index.html` |

Sem make: `docker compose --profile app up --build -d` sobe tudo e `./mvnw verify` roda testes e cobertura.

## Autenticação

Todas as rotas exigem token JWT, menos o login, o Swagger e o health.

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"login":"admin","senha":"<DEMO_PASSWORD do .env>"}'
```

O token vem em `accessToken`, vale 1 hora e vai no cabeçalho `Authorization: Bearer <token>`. No Swagger, use o botão Authorize. No profile `dev` existem os usuários `admin`, `atendente` e `mecanico`, todos com a senha de `DEMO_PASSWORD`.

O token carrega o papel do usuário (ADMIN, ATENDENTE ou MECANICO). Hoje só o Actuator exige ADMIN. O login tem limite de 30 tentativas por minuto por IP.

## Endpoints

| Método | Rota | O que faz |
|---|---|---|
| POST | `/api/auth/login` | devolve o token JWT |
| POST | `/api/ordens-servico` | abre uma ordem de serviço para um cliente e um veículo |
| POST | `/api/orcamentos` | gera o orçamento de uma ordem de serviço |
| GET | `/api/orcamentos/{id}` | consulta um orçamento |
| PATCH | `/api/orcamentos/{id}/aprovar` | aprova o orçamento |
| PATCH | `/api/orcamentos/{id}/rejeitar` | rejeita o orçamento |

A documentação completa, com exemplos, fica no Swagger.

## Erros

Todo erro volta no mesmo formato:

```json
{
  "status": 404,
  "erro": "Recurso Não Encontrado",
  "mensagem": "Cliente não encontrado.",
  "timestamp": "2026-10-07T10:15:30"
}
```

| Status | Quando |
|---|---|
| 400 | campo inválido (a mensagem lista os campos), JSON mal formado ou parâmetro faltando |
| 401 | sem token, token inválido ou login errado |
| 403 | o papel do usuário não permite |
| 404 | recurso ou rota inexistente |
| 405 e 415 | método ou formato não aceito |
| 409 | conflito no banco (registro duplicado ou alterado por outra operação) |
| 422 | regra de negócio violada |
| 429 | excesso de tentativas no login |
| 500 | erro inesperado, sem expor detalhe interno |

No código, não encontrado é `RecursoNaoEncontradoException` e regra violada é `RegraNegocioException`. O `TratadorGlobalDeExcecoes` faz a tradução para HTTP, e nenhum controller trata erro por conta própria. A decisão está no [ADR 0005](docs/arquitetura/adr/0005-padrao-de-erros.md).

## Arquitetura

Monólito modular com Clean Architecture. É uma aplicação só, com um deploy e um banco, dividida pelos contextos que saíram do Event Storming: autenticação, cliente, veículo, catálogo, estoque e ordem de serviço.

Cada contexto tem três camadas, e a dependência sempre aponta para dentro:

- `domain`: as regras do negócio, como as transições de status e o cálculo do orçamento. Não depende de Spring nem de banco.
- `application`: os casos de uso (`*UseCase.execute`), os DTOs (`*Input` e `*Output`) e as interfaces do que o caso de uso precisa de fora.
- `infrastructure`: controllers REST, entidades JPA (`*Entity`), mappers (`*Mapper`) e adapters (`*RepositoryAdapter`).

O domínio declara interfaces (por exemplo `OrcamentoRepository`) e a infraestrutura entrega a implementação (`OrcamentoRepositoryAdapter`). Testes de arquitetura com ArchUnit reprovam o build se alguém quebrar o padrão de erros.

As decisões estão em [docs/arquitetura/adr](docs/arquitetura/adr) e os diagramas em [docs/arquitetura/c4.md](docs/arquitetura/c4.md).

### Estrutura

```
src/main/java/br/com/fiap/oficina_mecanica/
├── autenticacao/      usuários, papéis, login e emissão do JWT
├── cliente/           clientes
├── veiculo/           veículos
├── catalogo/          serviços oferecidos pela oficina (ainda sem código)
├── estoque/           peças e insumos (ainda sem código)
├── ordemservico/      ordem de serviço e orçamento
└── compartilhado/     exceções, tratamento de erros, Swagger e limite de requisições

src/main/resources/
├── application*.properties   configuração por profile
└── db/migration, db/seed     migrations e dados de exemplo do Flyway

docs/                  arquitetura (ADRs e C4), segurança e DDD
scripts/               smoke test
```

## Banco de dados

PostgreSQL 16. Os dados da oficina são relacionais por natureza: cliente tem veículos, veículo tem ordens de serviço, ordem tem orçamento com serviços e peças. Chaves estrangeiras e restrições de unicidade barram inconsistências, e as transações garantem que um fluxo que falha no meio é desfeito por inteiro. MongoDB e MySQL foram considerados e descartados ([ADR 0002](docs/arquitetura/adr/0002-postgresql.md)).

O schema é versionado com Flyway, e o Hibernate só valida (`ddl-auto=validate`):

| Migration | Conteúdo |
|---|---|
| V1 | orçamentos e itens de serviço e de peça |
| V2 | clientes |
| V3 | veículos |
| V4 | ordens de serviço |
| V5 | usuários |

Os dados de exemplo ficam em `db/seed/dev` e só rodam no profile `dev`.

### Profiles

| Profile | Banco | Uso |
|---|---|---|
| `dev` (padrão) | Postgres do Docker | desenvolvimento, com dados de exemplo e usuários de demonstração |
| `prd` | `DB_URL`, `DB_USER` e `DB_PASSWORD` obrigatórios | produção |
| `test` | H2 em memória, modo PostgreSQL | testes automatizados |

## Testes e qualidade

- `make verify` roda testes unitários, de integração e de arquitetura. O build falha se a cobertura de linhas do projeto inteiro ficar abaixo de 80%.
- O Trivy procura vulnerabilidades nas dependências, segredos no código e erros no Dockerfile. O resultado está no [relatório de vulnerabilidades](docs/seguranca/relatorio-vulnerabilidades.md).

## CI e fluxo de trabalho

Todo PR para `dev` ou `main` passa por estes checks, todos obrigatórios:

| Check | O que confere |
|---|---|
| Testes, arquitetura e cobertura mínima de 80% | `./mvnw verify` |
| Padrão dos imports | sem `*`, sem import sem uso e na ordem do IntelliJ, só nos arquivos alterados |
| Vulnerabilidades, segredos e Dockerfile | Trivy, falha com crítica ou alta que já tem correção |
| Título do PR, branch e commits | formato `tipo: descrição` e branch `tipo/assunto` |
| Origem do PR para a main | a `main` só recebe PR vindo da `dev` |

O CodeQL do GitHub também analisa o código e os workflows. O fluxo de branches, as regras de merge e o que conferir antes do PR estão no [CONTRIBUTING.md](CONTRIBUTING.md).
