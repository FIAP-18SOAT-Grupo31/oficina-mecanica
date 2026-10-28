# Oficina Mecânica: sistema de atendimento e execução de serviços

Back-end do Tech Challenge da fase 1 da pós SOAT (FIAP), grupo 31.

## Objetivo

A oficina controla atendimentos, diagnósticos, orçamentos e estoque em planilhas e anotações. Isso gera erro de prioridade, falha no controle de peças, perda de histórico e um fluxo de aprovação lento. O objetivo deste MVP é organizar esse processo num sistema único: da chegada do veículo à entrega, com o cliente acompanhando e aprovando o orçamento pela API.

## Descrição da solução

- **Ordem de serviço (OS):** aberta para um cliente e um veículo cadastrados. O status muda conforme as ações no sistema: Recebida, Em diagnóstico, Aguardando aprovação, Aprovada, Em execução, Finalizada e Entregue. Também pode terminar como Recusada ou Cancelada. Cada mudança fica no histórico da OS.
- **Orçamento:** montado com serviços do catálogo e peças do estoque. O total é calculado automaticamente e as peças ficam reservadas até a decisão. O cliente aprova tudo, aprova só parte dos itens ou rejeita. A aprovação dá baixa no estoque, e a rejeição devolve as peças reservadas.
- **Acompanhamento pelo cliente:** ao abrir a OS, o cliente recebe por e-mail um código de acesso. Com o CPF/CNPJ e esse código, ele consulta a OS e decide o orçamento nas rotas públicas `/api/publico/**`, sem login. No MVP, o envio de e-mail é simulado e a mensagem aparece no log da aplicação.
- **Gestão administrativa:** CRUD de clientes, veículos, serviços e peças/insumos com controle de estoque, listagem e detalhe de OS e tempo médio de execução dos serviços.
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
| catálogo de serviços | Serviços oferecidos e valor da mão de obra |
| `autenticacao` | Login, emissão do JWT e usuários |
| `compartilhado` | Paginação, exceções, publicação de eventos e padrão de erros |

Os módulos conversam por portas (por exemplo, `VerificadorCliente` e `ConsultorVeiculos`). Testes ArchUnit garantem o padrão de erros da API (um único tratador global e exceções de domínio sem dependência de framework). Eventos de domínio, como `OrdemServicoCriada` e `OrcamentoGerado`, são publicados depois do commit e disparam as notificações ao cliente.

A modelagem de domínio (Event Storming, Domain Storytelling, agregados, contextos e linguagem ubíqua) está em [`docs/ddd`](docs/ddd).

## Tecnologias

- Java 21 e Spring Boot 4.1 (Web MVC, Data JPA, Validation, Security com OAuth2 Resource Server/JWT, Actuator)
- PostgreSQL 16 com migrations Flyway
- springdoc-openapi (Swagger UI)
- JUnit 5, Mockito, AssertJ, H2 (testes), Testcontainers, ArchUnit e JaCoCo
- Docker e Docker Compose
- GitHub Actions: build, testes e cobertura, Spotless, Trivy e CodeQL

## Pré-requisitos

- Docker e Docker Compose. É o suficiente para subir tudo em containers.
- Para rodar fora do container ou executar os testes: JDK 21. O Maven não precisa ser instalado, o projeto usa o wrapper `./mvnw`.
- Porta 5432 livre para o PostgreSQL. Se já houver um PostgreSQL instalado na máquina, pare o serviço ou troque a porta no `docker-compose.yml` e ajuste `DB_URL` no `.env`.

## Como executar

1. Crie o `.env` a partir do exemplo e troque os valores:

   ```bash
   cp .env.example .env
   ```

   | Variável | Uso |
   |---|---|
   | `POSTGRES_USER`, `POSTGRES_PASSWORD` | Usuário e senha do banco |
   | `JWT_SECRET` | Chave de assinatura do JWT, com 32 caracteres ou mais |
   | `DEMO_PASSWORD` | Senha dos usuários de demonstração. Sem ela, os usuários não são criados |
   | `APP_PROFILES` | Perfil do Spring. `dev` carrega os dados de demonstração |
   | `PGADMIN_EMAIL`, `PGADMIN_PASSWORD` | Acesso ao pgAdmin |

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

Também há atalhos no `Makefile`: `make up`, `make down`, `make logs`, `make run` e `make smoke`. O último faz um teste rápido dos endpoints com a aplicação no ar.

## Como executar os testes

```bash
./mvnw verify
```

Esse comando roda os testes unitários e de integração, os testes de arquitetura (ArchUnit) e a verificação de cobertura do JaCoCo. O build falha se a cobertura de linhas ficar abaixo de 80%. O relatório fica em `target/site/jacoco/index.html`.

Os testes de integração usam H2 em modo PostgreSQL. O teste contra um PostgreSQL real (`PostgresRealIntegrationTest`) usa Testcontainers e é pulado automaticamente quando não há Docker. No GitHub Actions, o mesmo comando roda em todo PR e o relatório de cobertura fica disponível como artefato.

## Documentação da API (Swagger)

Com a aplicação no ar:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI (JSON): `http://localhost:8080/v3/api-docs`

Para chamar as rotas protegidas no Swagger, faça o login (abaixo), clique em **Authorize** e cole o `accessToken`.

## Banco de dados: PostgreSQL

Escolhemos o PostgreSQL porque o domínio é relacional e transacional:

- **Transações:** aprovar um orçamento altera o orçamento, a OS e o saldo de várias peças de uma vez. Isso precisa ser atômico, e o PostgreSQL dá transações ACID.
- **Integridade:** relações fortes entre cliente, veículo, OS, orçamento e itens. Chaves estrangeiras e restrições `UNIQUE` (documento, placa, nome da peça) garantem a consistência no próprio banco.
- **Ecossistema:** consultas com filtros e paginação, tipos `UUID` e `NUMERIC` para valores, suporte maduro no Spring Data JPA e no Flyway, imagem Docker oficial e uso gratuito.

O esquema é versionado por migrations Flyway em `src/main/resources/db/migration`.

## Usuários e autenticação

As rotas administrativas exigem `Authorization: Bearer <token>`. Quando `DEMO_PASSWORD` está definida, a aplicação cria dois usuários de demonstração, ambos com a senha de `DEMO_PASSWORD` (no `.env.example` é `dev123`):

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

No perfil `dev`, a aplicação também carrega dados de demonstração: a cliente Maria da Silva (CPF `52998224725`), um veículo `ABC1D23` e uma OS aguardando aprovação com o código de acesso `DEMO12`. Com esses dados dá para testar a área do cliente sem login:

```bash
curl -X POST http://localhost:8080/api/publico/ordens-servico/0192f0a0-0000-7000-8000-0000000000a1/consultar \
  -H 'Content-Type: application/json' \
  -d '{"cpf":"52998224725","codigoAcesso":"DEMO12"}'
```

Abertos sem token ficam só o login, as rotas `/api/publico/**`, o Swagger e o health check. O login e as rotas públicas têm limite de requisições por minuto por IP.

## Estrutura de diretórios

```
.
├── src/main/java/br/com/fiap/oficina_mecanica
│   ├── ordemservico/      # OS e orçamento (domain, application, infrastructure)
│   ├── cliente/
│   ├── veiculo/
│   ├── estoque/
│   ├── autenticacao/
│   └── compartilhado/
├── src/main/resources
│   ├── db/migration/      # migrations Flyway
│   └── db/seed/dev/       # dados de demonstração (perfil dev)
├── src/test/java/...      # testes unitários, de integração e de arquitetura
├── docs
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

O relatório da análise de vulnerabilidades está em [`docs/seguranca/relatorio-vulnerabilidades.md`](docs/seguranca/relatorio-vulnerabilidades.md).

## Vídeo da entrega

Link do vídeo no YouTube: **a definir**.
