# ADR 0001: Monólito modular em camadas

Status: aceito (25/09/2026, commit `0890f41`)

## Contexto

O enunciado pede um back-end monolítico e sugere arquitetura em camadas para o MVP. Ao mesmo tempo, o Event Storming mostrou contextos bem separados (atendimento e OS, cadastro de cliente e veículo, estoque, catálogo de serviços) e o grupo trabalha em paralelo, cada um em um pedaço do domínio. Queríamos que um PR de estoque não precisasse mexer em arquivos de OS.

## Decisão

Um único projeto Spring Boot, dividido em módulos por contexto (`ordemservico`, `cliente`, `veiculo`, `estoque`, `servico`, `autenticacao` e `compartilhado`). Dentro de cada módulo, três camadas:

- `domain`: entidades, objetos de valor, regras e interfaces de repositório, sem Spring nem JPA;
- `application`: casos de uso (`*UseCase`) e DTOs, com o `@Transactional`;
- `infrastructure`: controllers, entidades JPA, adaptadores e configuração.

O `ordemservico` acessa cliente e veículo por portas próprias (`VerificadorCliente`, `VerificadorVeiculo`, `ConsultorClientes`, `ConsultorVeiculos`), implementadas por adaptadores. Os avisos ao cliente saem de eventos de domínio (`OrdemServicoCriada`, `OrcamentoGerado`, `OrdemServicoAprovada` etc.) publicados por `PublicadorEventos` e tratados com `@TransactionalEventListener(AFTER_COMMIT)`.

## Alternativas consideradas

- Microsserviços: separação maior, mas exige mais de um deploy, comunicação pela rede e consistência entre bancos. Não faz sentido para um MVP, e o enunciado pede monólito.
- Camadas globais (`controller/`, `service/`, `repository/` na raiz): mais simples no começo, mas mistura os contextos e gera conflito de merge entre os PRs.
- Arquitetura hexagonal completa, com portas para tudo: mais isolamento, mas muito código de adaptador para o tamanho do projeto.

## Consequências

- Cada contexto fica numa pasta, e os PRs do grupo quase não se cruzam.
- Um único deploy, um único banco e transações locais (ver [ADR 0004](0004-baixa-de-estoque-na-transacao-do-orcamento.md)).
- Débito técnico: o `ordemservico` usa direto os repositórios de domínio de `estoque` e `servico`, e alguns adaptadores usam repositórios JPA de outro módulo. Os testes ArchUnit hoje só conferem o padrão de erros, não as dependências entre camadas e módulos.
