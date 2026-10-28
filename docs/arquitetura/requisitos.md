# Requisitos

Requisitos do enunciado da fase 1 e onde cada um está no código. Todas as rotas de `/api` fora `/api/auth/**` e `/api/publico/**` exigem JWT com papel `ADMIN` ou `ATENDENTE`.

## Requisitos funcionais

| # | Requisito | Endpoints | Situação |
|---|---|---|---|
| RF01 | Identificar o cliente por CPF/CNPJ ao abrir a OS | `POST /api/ordens-servico` com `documentoCliente` (ou `clienteId`) | atendido |
| RF02 | Cadastro de veículo (placa, marca, modelo, ano) e uso da placa na OS | `/api/veiculos`; `POST /api/ordens-servico` com `placaVeiculo` (ou `veiculoId`) | atendido |
| RF03 | Incluir serviços do catálogo e peças/insumos no orçamento | `POST /api/orcamentos` (`servicos[].catalogoServicoId`, `pecas[].produtoEstoqueId`) | atendido |
| RF04 | Orçamento calculado automaticamente | `POST /api/orcamentos` (total = mão de obra + peças) | atendido |
| RF05 | Enviar o orçamento ao cliente | evento `OrcamentoGerado`, e-mail simulado no log | atendido (e-mail simulado) |
| RF06 | Aprovar, aprovar em parte ou rejeitar o orçamento | `POST /api/publico/orcamentos/{id}/aprovar` e `/rejeitar`; `PATCH /api/orcamentos/{id}/aprovar` e `/rejeitar` | atendido |
| RF07 | Status da OS mudando conforme as ações | `PATCH /api/ordens-servico/{id}/iniciar-execucao`, `/finalizar`, `/entregar`, `/cancelar`; geração e decisão do orçamento | atendido |
| RF08 | Cliente acompanhar a OS pela API | `POST /api/publico/ordens-servico/{id}/consultar`, `POST /api/publico/orcamentos/{id}/consultar` | atendido |
| RF09 | CRUD de clientes | `POST`, `GET`, `GET /{id}`, `PUT /{id}`, `DELETE /{id}` em `/api/clientes` | atendido |
| RF10 | CRUD de veículos | `POST`, `GET`, `GET /{id}`, `PUT /{id}`, `DELETE /{id}` em `/api/veiculos` | atendido |
| RF11 | CRUD de serviços | `POST`, `GET`, `GET /{id}`, `PUT /{id}`, `DELETE /{id}` em `/api/servicos` | atendido |
| RF12 | CRUD de peças e insumos com controle de estoque | `POST /api/estoque`, `GET /api/estoque/{id}`, `GET /api/estoque/peca?nomePeca=`, `POST /api/estoque/{id}/adicionar` e `/remover`; reserva e baixa pelo orçamento | parcial: falta listar, atualizar e excluir peça |
| RF13 | Listagem e detalhe de OS | `GET /api/ordens-servico?status=`, `GET /api/ordens-servico/{id}` | atendido |
| RF14 | Tempo médio de execução dos serviços | nenhum; as datas ficam em `historico_status_ordem_servico` | pendente |
| RF15 | Login da equipe | `POST /api/auth/login` | atendido |

## Requisitos não funcionais

| # | Requisito | Como está |
|---|---|---|
| RNF01 | Segurança das APIs administrativas | JWT HS256 com validade de 1 hora, papéis `ADMIN` e `ATENDENTE` com `@PreAuthorize`, senhas em BCrypt ([ADR 0003](adr/0003-autenticacao-jwt.md)) |
| RNF02 | Validação de dados sensíveis | CPF/CNPJ pelos dígitos verificadores (`Cliente`), placa no padrão antigo ou Mercosul (`Veiculo`), Bean Validation nos DTOs |
| RNF03 | Cobertura de testes de 80% ou mais | JaCoCo no `./mvnw verify` falha abaixo de 80% de linhas; roda em todo PR no GitHub Actions |
| RNF04 | Consistência das operações | transação por caso de uso (`@Transactional`); reserva e baixa de estoque na mesma transação do orçamento ([ADR 0004](adr/0004-baixa-de-estoque-na-transacao-do-orcamento.md)) |
| RNF05 | Rodar em qualquer máquina com Docker | `Dockerfile` multi-stage e `docker-compose.yml` com PostgreSQL, pgAdmin e a aplicação (perfil `app`) |
| RNF06 | API documentada | springdoc-openapi: Swagger UI em `/swagger-ui.html` e OpenAPI em `/v3/api-docs`, com o esquema `bearer-jwt` |
| RNF07 | Proteção contra abuso nas rotas abertas | `LimiteRequisicoesFilter`: limite por minuto e por IP em `/api/auth/login` e `/api/publico/**` (padrão 30) |
| RNF08 | Health check | `/actuator/health/**` aberto, `HEALTHCHECK` no `Dockerfile` e `pg_isready` no serviço `db` do compose |
| RNF09 | Análise de vulnerabilidades | Trivy e CodeQL em todo PR ([relatório](../seguranca/relatorio-vulnerabilidades.md)) |
