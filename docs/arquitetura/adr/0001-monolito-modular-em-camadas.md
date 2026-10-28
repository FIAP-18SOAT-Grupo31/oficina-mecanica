# ADR 0001: Monólito modular em camadas

- **Data:** 25/09/2026
- **Status:** Aceita
- **Responsável:** Ruan Pablo (commit `0890f41`, setup inicial da arquitetura)

## Contexto

O enunciado pede um back-end monolítico e sugere arquitetura em camadas para o MVP. Ao mesmo tempo, o Event Storming mostrou contextos bem separados (atendimento e OS, cadastro de cliente e veículo, estoque, catálogo de serviços) e o grupo trabalha em paralelo, cada um em um pedaço do domínio. Queríamos que um PR de estoque não precisasse mexer em arquivos de OS.

## Decisão

Montamos um único projeto Spring Boot e o dividimos em módulos por contexto: `ordemservico`, `cliente`, `veiculo`, `estoque`, `servico`, `autenticacao` e `compartilhado`. O `compartilhado` guarda só utilitários técnicos (paginação, exceções, publicação de eventos e padrão de erros), não um modelo de domínio comum.

Cada módulo tem três camadas. A `domain` concentra entidades, objetos de valor, regras e interfaces de repositório, sem Spring nem JPA. A `application` guarda os casos de uso (`*UseCase`) e os DTOs, e é nela que fica o `@Transactional`. A `infrastructure` reúne controllers, entidades JPA, adaptadores e configuração.

O `ordemservico` acessa cliente e veículo por portas próprias (`VerificadorCliente`, `VerificadorVeiculo`, `ConsultorClientes`, `ConsultorVeiculos`), que os adaptadores implementam. Os avisos ao cliente saem de eventos de domínio (`OrdemServicoCriada`, `OrcamentoGerado`, `OrdemServicoAprovada` etc.), que o `PublicadorEventos` publica e listeners com `@TransactionalEventListener(AFTER_COMMIT)` tratam.

## Alternativas consideradas

- Microsserviços: separação maior, mas exige mais de um deploy, comunicação pela rede e consistência entre bancos. Não faz sentido para um MVP, e o enunciado pede monólito.
- Camadas globais (`controller/`, `service/`, `repository/` na raiz): mais simples no começo, mas mistura os contextos e gera conflito de merge entre os PRs.
- Arquitetura hexagonal completa, com portas para tudo: mais isolamento, mas muito código de adaptador para o tamanho do projeto.

## Consequências

Positivas:

- Cada contexto fica numa pasta, e os PRs do grupo quase não se cruzam.
- Um único deploy, um único banco e transações locais (ver [ADR 0004](0004-baixa-de-estoque-na-transacao-do-orcamento.md)).

Negativas:

- O `ordemservico` usa direto os repositórios de domínio de `estoque` e `servico`, e alguns adaptadores usam repositórios JPA de outro módulo, então o isolamento entre módulos depende de disciplina.
- Fica para a próxima fase: os testes ArchUnit hoje só conferem o padrão de erros, não as dependências entre camadas e módulos.
