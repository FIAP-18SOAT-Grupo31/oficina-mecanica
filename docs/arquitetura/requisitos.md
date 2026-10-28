# Requisitos

Requisitos da fase 1 com ID e prioridade MoSCoW (Must, Should, Could, Won't).

Todas as rotas de `/api`, menos `/api/auth/**` e `/api/publico/**`, exigem JWT com papel `ADMIN` ou `ATENDENTE`.

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

| ID | Requisito | MoSCoW | Métrica / como verificar | Onde está configurado |
|---|---|---|---|---|
| RNF01 | O sistema deve proteger as APIs administrativas com JWT e papéis. | Must | sem token: 401; papel sem permissão: 403; o token expira em 1 hora; a aplicação não sobe com `JWT_SECRET` de menos de 32 caracteres | `app.seguranca.jwt.validade=1h` em `application.properties`; `PropriedadesJwt` |
| RNF02 | O sistema deve validar dados sensíveis na entrada. | Must | recusa CPF/CNPJ com dígito verificador inválido e placa fora do padrão antigo (ABC1234) ou Mercosul (ABC1D23) | `Cliente`, `Veiculo`, Bean Validation nos DTOs |
| RNF03 | O código deve ter cobertura de testes de pelo menos 80%. | Must | `./mvnw verify` falha com menos de 80% de linhas cobertas no projeto todo (regra `BUNDLE`, contador `LINE`); roda em todo PR | `jacoco.cobertura.minima=0.80` no `pom.xml`; job `build` do `ci.yml` |
| RNF04 | O sistema deve manter orçamento, OS e estoque consistentes. | Must | se uma peça do orçamento não tiver saldo, nenhuma reserva é gravada e a resposta é 422 | `@Transactional` nos casos de uso |
| RNF05 | O sistema deve rodar em qualquer máquina com Docker. | Must | `docker compose --profile app up` sobe aplicação, PostgreSQL 16 e pgAdmin | `Dockerfile` multi-stage, `docker-compose.yml` |
| RNF06 | A API deve ser documentada. | Should | `/v3/api-docs` e `/swagger-ui.html` respondem sem token, com o esquema `bearer-jwt` | springdoc-openapi |
| RNF07 | O sistema deve limitar requisições nas rotas abertas. | Must | até 30 requisições por minuto por IP, com um contador para `/api/auth/login` e outro para todas as rotas `/api/publico/**` juntas (janela fixa de 1 minuto, em memória); acima disso, 429 com `Retry-After` | `app.seguranca.limite-requisicoes.por-minuto` (padrão 30, variável `LIMITE_REQUISICOES_POR_MINUTO`) |
| RNF08 | O sistema deve expor health check. | Should | `/actuator/health` responde sem token; `HEALTHCHECK` no `Dockerfile`; `pg_isready` no serviço `db` | `SegurancaConfig`, `Dockerfile`, `docker-compose.yml` |
| RNF09 | O build deve barrar vulnerabilidades conhecidas. | Must | o Trivy falha o CI com CVE CRITICAL ou HIGH que já tenha correção; CodeQL analisa todo PR ([relatório](../seguranca/relatorio-vulnerabilidades.md)) | `ci.yml` (Trivy); CodeQL pela configuração padrão do GitHub |
| RNF10 | As listagens devem ser paginadas. | Should | no máximo 100 itens por página; tamanho fora de 1 a 100 é recusado | `Paginacao.TAMANHO_MAXIMO` |

## Rastreabilidade dos fluxos principais

| Fluxo | Requisitos | Endpoint | Teste |
|---|---|---|---|
| Abrir OS | RF01, RF02 | `POST /api/ordens-servico` | `OrdemServicoIntegrationTest`, `CriarOrdemServicoUseCaseTest` |
| Gerar orçamento | RF03, RF04, RNF04 | `POST /api/orcamentos` | `OrcamentoUseCasesTest` |
| Aprovar ou rejeitar orçamento | RF06 | `POST /api/publico/orcamentos/{id}/aprovar` e `/rejeitar` | `OrcamentoUseCasesTest`, `OrdemServicoIntegrationTest` |
| Acompanhar OS | RF08 | `POST /api/publico/ordens-servico/{id}/consultar` | `OrdemServicoIntegrationTest`, `ConsultarOrdemServicoClienteUseCaseTest` |
| Avançar status da OS | RF07 | `PATCH /api/ordens-servico/{id}/iniciar-execucao`, `/finalizar`, `/entregar`, `/cancelar` | `OrdemServicoCicloDeVidaTest`, `OrdemServicoIntegrationTest` |
| Login | RF15, RNF01 | `POST /api/auth/login` | `SegurancaIntegrationTest` |

## Critérios de aceitação

### Criar OS

**Dado** um cliente com CPF cadastrado e um veículo dele, **quando** o atendente envia `POST /api/ordens-servico` com `documentoCliente`, `placaVeiculo` e o relato do problema, **então** a resposta é 201, a OS fica `RECEBIDA` e o cliente recebe o código de acesso.

### Aprovar orçamento

**Dado** um orçamento `PENDENTE` dentro da validade, **quando** o cliente envia `POST /api/publico/orcamentos/{id}/aprovar` com CPF e código de acesso, **então** o orçamento fica `APROVADO`, a OS fica `APROVADA` e as peças saem do estoque.

### Acompanhar OS

**Dado** uma OS aberta, **quando** o cliente envia `POST /api/publico/ordens-servico/{id}/consultar` com o CPF e o código de acesso, **então** a resposta é 200 com o status da OS, o veículo e o resumo do último orçamento.

## Pendências

- RF12: faltam listar, atualizar e excluir peça.
- RF14: tempo médio de execução ainda não foi implementado.
