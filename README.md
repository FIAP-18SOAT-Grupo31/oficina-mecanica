# Oficina Mecânica

Back-end do Tech Challenge da fase 1 da pós SOAT (FIAP), grupo 31.

## Objetivo

A oficina controla atendimentos, diagnósticos, orçamentos e estoque em planilhas e anotações. Isso gera erro de prioridade, falha no controle de peças, perda de histórico e um fluxo de aprovação lento. O objetivo deste MVP é organizar esse processo num sistema único: da chegada do veículo à entrega, com o cliente acompanhando e aprovando o orçamento pela API.

## Descrição da solução

- **Ordem de serviço (OS):** aberta para um cliente e um veículo cadastrados, identificados pelo id ou pelo CPF/CNPJ do cliente e pela placa do veículo. O status muda conforme as ações no sistema: Recebida, Em diagnóstico, Aguardando aprovação, Aprovada, Em execução, Finalizada e Entregue. Também pode terminar como Recusada ou Cancelada. Cada mudança fica no histórico da OS.
- **Orçamento:** montado com serviços do catálogo e peças do estoque. O total é calculado automaticamente e as peças ficam reservadas até a decisão. O cliente aprova tudo, aprova só parte dos itens ou rejeita. A aprovação dá baixa no estoque, e a rejeição devolve as peças reservadas.
- **Acompanhamento pelo cliente:** ao abrir a OS, o cliente recebe por e-mail um código de acesso. Com o CPF/CNPJ e esse código, ele consulta a OS e decide o orçamento nas rotas públicas `/api/publico/**`, sem login. No MVP, o envio de e-mail é simulado e a mensagem aparece no log da aplicação.
- **Gestão administrativa:** CRUD de clientes, veículos e serviços do catálogo; cadastro e consulta de peças/insumos com entrada e saída de estoque; listagem e detalhe de OS. O histórico de status já grava a data de cada mudança, mas a consulta do tempo médio de execução ainda não foi implementada.
- **Segurança:** as APIs administrativas exigem JWT. Os papéis são `ADMIN` e `ATENDENTE`. CPF/CNPJ é validado pelos dígitos verificadores e a placa no padrão antigo ou Mercosul.

## Arquitetura

Monólito modular em camadas, organizado por contexto de domínio (DDD). Cada módulo tem três camadas:

- `domain`: entidades, objetos de valor, regras e portas (interfaces de repositório). Não depende de Spring nem de JPA.
- `application`: casos de uso e DTOs. Orquestra o domínio e controla as transações.
- `infrastructure`: controllers REST, entidades JPA, adaptadores das portas e configuração.

| Módulo | Responsabilidade |
|---|---|
| `ordemservico` | Núcleo do domínio: ordem de serviço, orçamento, histórico de status, rotas públicas do cliente e notificações |
| `cliente` | Cadastro de clientes |
| `veiculo` | Cadastro de veículos |
| `estoque` | Peças e insumos: saldo disponível e reservado |
| `servico` | Catálogo de serviços: nome, descrição e valor da mão de obra |
| `autenticacao` | Login, emissão do JWT e usuários |
| `compartilhado` | Paginação, exceções, publicação de eventos e padrão de erros |

O `ordemservico` fala com `cliente` e `veiculo` por portas próprias, como `VerificadorCliente` e `VerificadorVeiculo`.

Estoque e catálogo são lidos direto pelos repositórios (`ProdutoEstoqueRepository`, `ServicoRepository`). Assim a reserva das peças fica na mesma transação do orçamento.

Os avisos ao cliente saem de eventos de domínio, como `OrdemServicoCriada` e `OrcamentoGerado`, tratados depois do commit.

Os testes ArchUnit conferem o padrão de erros da API.

- Diagramas C4: [`docs/arquitetura/c4.md`](docs/arquitetura/c4.md)
- ADRs: [`docs/arquitetura/adr`](docs/arquitetura/adr)
- Requisitos: [`docs/arquitetura/requisitos.md`](docs/arquitetura/requisitos.md)
- DDD: [`docs/ddd`](docs/ddd)

## Tecnologias

- Java 21 e Spring Boot 4.1 (Web MVC, Data JPA, Validation, Security com OAuth2 Resource Server/JWT, Actuator)
- PostgreSQL 16 com migrations Flyway
- springdoc-openapi (Swagger UI)
- JUnit 5, Mockito, AssertJ, H2 (testes), Testcontainers, ArchUnit e JaCoCo
- Docker e Docker Compose
- GitHub Actions: build, testes e cobertura, Spotless, Trivy e CodeQL

## Pré-requisitos

- Docker e Docker Compose.
- JDK 21 para rodar fora do container ou rodar os testes (o projeto usa o `./mvnw`).
- Porta 5432 livre. Se já tiver um PostgreSQL na máquina, pare o serviço ou troque a porta no `docker-compose.yml`.

## Como executar

1. Crie o `.env` a partir do exemplo e troque os valores:

   ```bash
   cp .env.example .env
   ```

   | Variável | Uso |
   |---|---|
   | `POSTGRES_USER`, `POSTGRES_PASSWORD` | Usuário e senha do banco |
   | `JWT_SECRET` | Chave de assinatura do JWT, com 32 caracteres ou mais |
   | `DEMO_PASSWORD` | Senha dos usuários de demonstração (ver [Usuários e autenticação](#usuários-e-autenticação)) |
   | `APP_PROFILES` | Perfil do Spring. `dev` carrega os dados de demonstração |
   | `PGADMIN_EMAIL`, `PGADMIN_PASSWORD` | Acesso ao pgAdmin |
   | `DB_URL` (opcional, fora do `.env.example`) | URL JDBC usada ao rodar fora do container. Padrão no perfil `dev`: `jdbc:postgresql://localhost:5432/oficina_mecanica`. No perfil `prd`, também `DB_USER` e `DB_PASSWORD` |

2. Suba o banco e a aplicação:

   ```bash
   docker compose --profile app up --build -d
   ```

   A API fica em `http://localhost:8080` e o pgAdmin em `http://localhost:5050`. Sem `--profile app`, sobem só o banco e o pgAdmin.

3. Para rodar a aplicação fora do container (com o banco do compose):

   ```bash
   ./mvnw spring-boot:run
   ```

   No perfil `dev`, o Spring Boot sobe o `docker-compose.yml` sozinho.

Também há atalhos no `Makefile`: `make up`, `make down`, `make logs` e `make run`. Com a aplicação no ar, `make smoke` roda `scripts/smoke-test.sh`, que confere:

- o OpenAPI e o Swagger UI respondem;
- o health check é público e o resto do Actuator pede token (401);
- o login recusa senha errada (401) e corpo incompleto (400);
- `admin` e `atendente` conseguem logar;
- o Actuator devolve 403 para `ATENDENTE`, 200 para `ADMIN` e 401 para token inválido.

## Como executar os testes

```bash
./mvnw verify
```

Roda os testes unitários, de integração e ArchUnit, e o JaCoCo. O build falha com cobertura de linhas abaixo de 80%. O relatório fica em `target/site/jacoco/index.html`.

Os testes de integração usam H2 em modo PostgreSQL. O `PostgresRealIntegrationTest` usa Testcontainers e é pulado quando não há Docker. O CI roda o mesmo comando em todo PR.

## Documentação da API (Swagger)

Com a aplicação no ar:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI (JSON): `http://localhost:8080/v3/api-docs`

Para chamar as rotas protegidas no Swagger, faça o login (abaixo), clique em **Authorize** e cole o `accessToken`.

Exemplo: abrir uma OS pelo CPF da cliente e pela placa do veículo, sem precisar dos ids:

```bash
curl -X POST http://localhost:8080/api/ordens-servico \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"documentoCliente":"529.982.247-25","placaVeiculo":"ABC1D23","relatoProblema":"Barulho ao frear na descida"}'
```

O cliente vai em `clienteId` **ou** `documentoCliente` (CPF/CNPJ, com ou sem pontuação), e o veículo em `veiculoId` **ou** `placaVeiculo`, um campo de cada par:

- se vierem os dois campos do par, ou nenhum, a resposta é 400;
- se o documento ou a placa não estiverem cadastrados, 404;
- se o veículo não for do cliente, 422.

## Banco de dados

PostgreSQL 16. Aprovar um orçamento mexe no orçamento, na OS e no estoque de uma vez, então precisamos de transação. FK e `UNIQUE` (documento, placa, nome da peça) ajudam a manter os cadastros consistentes. Detalhes no [ADR 0002](docs/arquitetura/adr/0002-banco-postgresql.md).

O esquema é versionado por migrations Flyway em `src/main/resources/db/migration`.

## Usuários e autenticação

As rotas administrativas exigem `Authorization: Bearer <token>`. A aplicação cria dois usuários de demonstração com a senha de `DEMO_PASSWORD`:

No perfil `dev` o padrão é `dev123`. Se a senha estiver vazia (por exemplo no `prd` sem a variável), os usuários não são criados.

| Login | Papel |
|---|---|
| `admin` | `ADMIN` |
| `atendente` | `ATENDENTE` |

Para obter o token:

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"login":"admin","senha":"dev123"}'
```

A resposta traz `accessToken`, `tokenType` e `expiresIn`. O token vale 1 hora.

No perfil `dev`, a aplicação também carrega dados de demonstração:

- a cliente Maria da Silva (CPF `52998224725`) e o veículo `ABC1D23`;
- o serviço "Troca de óleo" no catálogo e a peça "ÓLEO 5W30 (LITRO)" no estoque;
- uma OS aguardando aprovação, com código de acesso `DEMO12` e um orçamento pendente com 4 unidades da peça reservadas.

Para testar a área do cliente sem login:

```bash
curl -X POST http://localhost:8080/api/publico/ordens-servico/0192f0a0-0000-7000-8000-0000000000a1/consultar \
  -H 'Content-Type: application/json' \
  -d '{"cpf":"52998224725","codigoAcesso":"DEMO12"}'
```

Rotas abertas sem token (`SegurancaConfig`):

- `/api/auth/**` (login) e `/api/publico/**` (área do cliente);
- `/`, `/index.html` e `/logo.svg` (página inicial);
- `/error`;
- Swagger: `/swagger-ui.html`, `/swagger-ui/**` e `/v3/api-docs/**`;
- `/actuator/health/**` e `/actuator/prometheus` (este só se estiver em `ACTUATOR_EXPOSICAO`).

O resto do Actuator exige `ADMIN`. Login e rotas públicas têm limite de 30 requisições por minuto por IP (`LIMITE_REQUISICOES_POR_MINUTO`).

## Estrutura de diretórios

```
.
├── src/main/java/br/com/fiap/oficina_mecanica
│   ├── ordemservico/      # OS e orçamento (domain, application, infrastructure)
│   ├── cliente/
│   ├── veiculo/
│   ├── estoque/
│   ├── servico/           # catálogo de serviços
│   ├── autenticacao/
│   └── compartilhado/
├── src/main/resources
│   ├── db/migration/      # migrations Flyway
│   └── db/seed/dev/       # dados de demonstração (perfil dev)
├── src/test/java/...      # testes unitários, de integração e de arquitetura
├── docs
│   ├── arquitetura/       # diagramas C4, ADRs e requisitos
│   ├── ddd/               # Event Storming, diagramas, linguagem ubíqua e histórias do egon.io
│   └── seguranca/         # relatório de vulnerabilidades
├── scripts/smoke-test.sh
├── Dockerfile
├── docker-compose.yml
└── Makefile
```

## Documentação DDD

- Board do Event Storming no Miro: https://miro.com/app/board/uXjVHnerRBQ=/?share_link_id=849086151688
- Exportação do Event Storming: [`docs/ddd/event-storming.pdf`](docs/ddd/event-storming.pdf)
- Domain Storytelling, agregados, contextos e máquinas de estado: [`docs/ddd/diagramas.pdf`](docs/ddd/diagramas.pdf)
- Histórias originais do egon.io: [`docs/ddd/egon`](docs/ddd/egon)
- Linguagem ubíqua e relação com o código: [`docs/ddd/linguagem-ubiqua.md`](docs/ddd/linguagem-ubiqua.md)

## Segurança

Relatório de vulnerabilidades: [`docs/seguranca/relatorio-vulnerabilidades.md`](docs/seguranca/relatorio-vulnerabilidades.md).

## Vídeo da entrega

Link do vídeo no YouTube: **a definir**.
