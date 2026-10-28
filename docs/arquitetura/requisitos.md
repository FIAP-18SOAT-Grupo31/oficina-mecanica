# Requisitos

Requisitos do enunciado da fase 1, no formato da aula de requisitos funcionais e não funcionais: cada requisito tem um ID, prioridade MoSCoW (Must, Should, Could, Won't), a situação no código e, na matriz de rastreabilidade, o endpoint, o teste que o verifica e o ADR quando houver.

Todas as rotas de `/api` fora `/api/auth/**` e `/api/publico/**` exigem JWT com papel `ADMIN` ou `ATENDENTE`.

## Requisitos funcionais

| ID | Requisito | MoSCoW | Situação |
|---|---|---|---|
| RF01 | O sistema deve identificar o cliente pelo CPF/CNPJ ao abrir a OS. | Must | atendido |
| RF02 | O sistema deve cadastrar o veículo (placa, marca, modelo, ano) e aceitar a placa na abertura da OS. | Must | atendido |
| RF03 | O sistema deve permitir incluir serviços do catálogo e peças/insumos do estoque no orçamento. | Must | atendido |
| RF04 | O sistema deve calcular o total do orçamento (mão de obra + peças). | Must | atendido |
| RF05 | O sistema deve enviar o orçamento ao cliente. | Must | atendido (e-mail simulado no log) |
| RF06 | O sistema deve permitir aprovar, aprovar em parte ou rejeitar o orçamento. | Must | atendido |
| RF07 | O sistema deve mudar o status da OS conforme as ações (diagnóstico, aprovação, execução, finalização, entrega, cancelamento). | Must | atendido |
| RF08 | O sistema deve permitir que o cliente acompanhe a OS pela API, sem login. | Must | atendido |
| RF09 | O sistema deve oferecer CRUD de clientes. | Must | atendido |
| RF10 | O sistema deve oferecer CRUD de veículos. | Must | atendido |
| RF11 | O sistema deve oferecer CRUD de serviços. | Must | atendido |
| RF12 | O sistema deve oferecer CRUD de peças e insumos com controle de estoque. | Must | parcial: faltam listar, atualizar e excluir peça |
| RF13 | O sistema deve listar e detalhar as OS, com filtro por status. | Must | atendido |
| RF14 | O sistema deve informar o tempo médio de execução dos serviços. | Should | pendente: as datas já ficam em `historico_status_ordem_servico`, mas não há cálculo nem endpoint |
| RF15 | O sistema deve autenticar a equipe da oficina por login e senha. | Must | atendido |
| RF16 | O sistema deve avisar o cliente por WhatsApp e gerar PDF da OS. | Won't | fora do escopo do MVP; o aviso é só o e-mail simulado |

## Requisitos não funcionais

Só entram métricas que dá para conferir no código ou no CI.

| ID | Requisito | MoSCoW | Métrica / como verificar | Onde está configurado |
|---|---|---|---|---|
| RNF01 | O sistema deve proteger as APIs administrativas com JWT e papéis. | Must | sem token: 401; papel sem permissão: 403; o token expira em 1 hora; a aplicação não sobe com `JWT_SECRET` de menos de 32 caracteres | `app.seguranca.jwt.validade=1h` em `application.properties`; `PropriedadesJwt` |
| RNF02 | O sistema deve validar dados sensíveis na entrada. | Must | recusa CPF/CNPJ com dígito verificador inválido e placa fora do padrão antigo (ABC1234) ou Mercosul (ABC1D23) | `Cliente`, `Veiculo`, Bean Validation nos DTOs |
| RNF03 | O código deve ter cobertura de testes de pelo menos 80%. | Must | `./mvnw verify` falha com menos de 80% de linhas cobertas no projeto todo (regra `BUNDLE`, contador `LINE`); roda em todo PR | `jacoco.cobertura.minima=0.80` no `pom.xml`; job `build` do `ci.yml` |
| RNF04 | O sistema deve manter orçamento, OS e estoque consistentes. | Must | se uma peça do orçamento não tiver saldo, nenhuma reserva é gravada e a resposta é 422 | `@Transactional` nos casos de uso |
| RNF05 | O sistema deve rodar em qualquer máquina com Docker. | Must | `docker compose --profile app up` sobe aplicação, PostgreSQL 16 e pgAdmin | `Dockerfile` multi-stage, `docker-compose.yml` |
| RNF06 | A API deve ser documentada. | Should | `/v3/api-docs` e `/swagger-ui.html` respondem sem token, com o esquema `bearer-jwt` | springdoc-openapi |
| RNF07 | O sistema deve limitar requisições nas rotas abertas. | Must | no máximo 30 requisições por minuto por IP e rota em `/api/auth/login` e `/api/publico/**`; acima disso, 429 com `Retry-After` | `app.seguranca.limite-requisicoes.por-minuto` (padrão 30, variável `LIMITE_REQUISICOES_POR_MINUTO`) |
| RNF08 | O sistema deve expor health check. | Should | `/actuator/health` responde sem token; `HEALTHCHECK` no `Dockerfile`; `pg_isready` no serviço `db` | `SegurancaConfig`, `Dockerfile`, `docker-compose.yml` |
| RNF09 | O build deve barrar vulnerabilidades conhecidas. | Must | o Trivy falha o CI com CVE CRITICAL ou HIGH que já tenha correção; CodeQL analisa todo PR ([relatório](../seguranca/relatorio-vulnerabilidades.md)) | `ci.yml` (Trivy); CodeQL pela configuração padrão do GitHub |
| RNF10 | As listagens devem ser paginadas. | Should | no máximo 100 itens por página; tamanho fora de 1 a 100 é recusado | `Paginacao.TAMANHO_MAXIMO` |

## Matriz de rastreabilidade

Requisito → endpoint → teste (classe em `src/test`) → ADR.

| ID | Endpoint | Teste | ADR |
|---|---|---|---|
| RF01 | `POST /api/ordens-servico` com `documentoCliente` (ou `clienteId`) | `OrdemServicoIntegrationTest`, `CriarOrdemServicoUseCaseTest` | - |
| RF02 | `/api/veiculos`; `POST /api/ordens-servico` com `placaVeiculo` (ou `veiculoId`) | `VeiculoControllerIntegrationTest`, `VeiculoTest`, `OrdemServicoIntegrationTest` | - |
| RF03 | `POST /api/orcamentos` (`servicos[].catalogoServicoId`, `pecas[].produtoEstoqueId`) | `OrcamentoUseCasesTest` | [0004](adr/0004-baixa-de-estoque-na-transacao-do-orcamento.md) |
| RF04 | `POST /api/orcamentos` | `OrcamentoUseCasesTest`, `OrcamentoTest` | - |
| RF05 | evento `OrcamentoGerado`, e-mail simulado no log | `OrdemServicoIntegrationTest`, `NotificadorOrdemServicoAdapterTest` | [0001](adr/0001-monolito-modular-em-camadas.md) |
| RF06 | `POST /api/publico/orcamentos/{id}/aprovar` e `/rejeitar`; `PATCH /api/orcamentos/{id}/aprovar` e `/rejeitar` | `OrcamentoUseCasesTest`, `OrdemServicoIntegrationTest` | [0004](adr/0004-baixa-de-estoque-na-transacao-do-orcamento.md) |
| RF07 | `PATCH /api/ordens-servico/{id}/iniciar-execucao`, `/finalizar`, `/entregar`, `/cancelar` | `OrdemServicoCicloDeVidaTest`, `AvancoDeStatusOrdemServicoUseCasesTest`, `CancelarOrdemServicoUseCaseTest`, `OrdemServicoIntegrationTest` | - |
| RF08 | `POST /api/publico/ordens-servico/{id}/consultar`, `POST /api/publico/orcamentos/{id}/consultar` | `OrdemServicoIntegrationTest`, `ConsultarOrdemServicoClienteUseCaseTest`, `VerificadorAcessoClienteAdapterTest` | [0003](adr/0003-autenticacao-jwt.md) |
| RF09 | `POST`, `GET`, `GET /{id}`, `PUT /{id}`, `DELETE /{id}` em `/api/clientes` | `ClienteControllerIntegrationTest`, `ClienteTest` | - |
| RF10 | `POST`, `GET`, `GET /{id}`, `PUT /{id}`, `DELETE /{id}` em `/api/veiculos` | `VeiculoControllerIntegrationTest` | - |
| RF11 | `POST`, `GET`, `GET /{id}`, `PUT /{id}`, `DELETE /{id}` em `/api/servicos` | `ServicoControllerIntegrationTest`, `ServicoUseCasesTest` | - |
| RF12 | `POST /api/estoque`, `GET /api/estoque/{id}`, `GET /api/estoque/peca?nomePeca=`, `POST /api/estoque/{id}/adicionar` e `/remover` | `ProdutoEstoqueTest`, `CriarProdutoEstoqueUseCaseTest`, `AdicionarEstoqueUseCaseTest`, `RemoverEstoqueUseCaseTest` (sem teste de integração do controller) | [0004](adr/0004-baixa-de-estoque-na-transacao-do-orcamento.md) |
| RF13 | `GET /api/ordens-servico?status=`, `GET /api/ordens-servico/{id}` | `OrdemServicoIntegrationTest`, `ListarOrdensServicoUseCaseTest`, `BuscarOrdemServicoUseCaseTest` | - |
| RF14 | nenhum | nenhum (pendente) | - |
| RF15 | `POST /api/auth/login` | `SegurancaIntegrationTest`, `AutenticarUsuarioUseCaseTest` | [0003](adr/0003-autenticacao-jwt.md) |
| RNF01 | todas as rotas administrativas | `SegurancaIntegrationTest`, `OrdemServicoIntegrationTest` | [0003](adr/0003-autenticacao-jwt.md) |
| RNF02 | `/api/clientes`, `/api/veiculos` | `ClienteTest`, `VeiculoTest` | - |
| RNF03 | - | `./mvnw verify` (JaCoCo) | - |
| RNF04 | `POST /api/orcamentos`, aprovação e rejeição | `OrcamentoUseCasesTest`, `OrdemServicoIntegrationTest` | [0004](adr/0004-baixa-de-estoque-na-transacao-do-orcamento.md) |
| RNF05 | - | `PostgresRealIntegrationTest` (Testcontainers) | [0002](adr/0002-banco-postgresql.md) |
| RNF06 | `/v3/api-docs`, `/swagger-ui.html` | `SegurancaIntegrationTest` | - |
| RNF07 | `/api/auth/login`, `/api/publico/**` | `LimiteRequisicoesFilterTest` | [0003](adr/0003-autenticacao-jwt.md) |
| RNF08 | `/actuator/health` | `SegurancaIntegrationTest`, `PostgresRealIntegrationTest` | - |
| RNF09 | - | job do Trivy no `ci.yml` | - |
| RNF10 | listagens (`GET /api/clientes`, `/api/veiculos`, `/api/ordens-servico` etc.) | `PaginacaoTest`, `OrdemServicoIntegrationTest` | - |

## Critérios de aceitação

### Criar OS (RF01, RF02)

- **Dado** um cliente com CPF cadastrado e um veículo dele com placa cadastrada, **quando** o atendente envia `POST /api/ordens-servico` com `documentoCliente`, `placaVeiculo` e o relato do problema, **então** o sistema responde 201 com o `Location` da OS, a OS fica `RECEBIDA` e o cliente recebe o aviso com o código de acesso.
- **Dado** um veículo que pertence a outro cliente, **quando** o atendente tenta abrir a OS, **então** o sistema responde 422 e não cria a OS.
- **Dado** um documento ou placa sem cadastro, **quando** o atendente tenta abrir a OS, **então** o sistema responde 404.

### Aprovar orçamento (RF06)

- **Dado** um orçamento `PENDENTE` dentro da validade, **quando** o cliente envia `POST /api/publico/orcamentos/{id}/aprovar` com CPF e código de acesso, **então** o orçamento fica `APROVADO`, a OS fica `APROVADA` e o cliente recebe o aviso.
- **Dado** um orçamento com vários itens, **quando** o cliente aprova só parte deles, **então** o orçamento fica `APROVADO_PARCIALMENTE` e o total é recalculado só com os itens aceitos.
- **Dado** um orçamento já aprovado, **quando** alguém tenta aprovar de novo, **então** o sistema responde 422.
- **Dado** um orçamento com a validade vencida, **quando** o cliente tenta aprovar, **então** o sistema responde com erro, o orçamento fica `EXPIRADO` e a OS fica `RECUSADA`.

### Acompanhar OS (RF08)

- **Dado** uma OS com orçamento pendente, **quando** o cliente envia `POST /api/publico/ordens-servico/{id}/consultar` com o CPF (com ou sem pontuação) e o código de acesso, **então** o sistema responde 200 com o status da OS, o veículo e o resumo do último orçamento, sem expor o código de acesso.
- **Dado** um código de acesso errado, **quando** o cliente consulta o orçamento, **então** o sistema responde 422 e não mostra os dados.

### Baixa de estoque (RF03, RF06, RF12)

- **Dado** uma peça com saldo disponível suficiente, **quando** o atendente gera um orçamento com essa peça, **então** a quantidade sai do disponível e vai para o reservado.
- **Dado** uma peça sem saldo suficiente, **quando** o atendente gera o orçamento, **então** o sistema responde 422 e nada é gravado.
- **Dado** um orçamento com peças reservadas, **quando** ele é aprovado, **então** as peças aprovadas saem do reservado (baixa) e as recusadas voltam ao disponível.
- **Dado** um orçamento com peças reservadas, **quando** ele é rejeitado ou a OS é cancelada, **então** todas as peças voltam ao disponível.

## Pendências

- RF12: listar, atualizar e excluir peça ainda não existem; o estoque também não tem teste de integração do controller.
- RF14: tempo médio de execução ainda não foi implementado.
