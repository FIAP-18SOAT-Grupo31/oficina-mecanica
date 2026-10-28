# ADR 0001: Monólito modular em camadas

- **Data:** 25/09/2026 (atualizado em 28/10/2026)
- **Status:** Aceita
- **Responsável:** Ruan Pablo

## Contexto

O enunciado pede um back-end monolítico e sugere arquitetura em camadas para o MVP. Ao mesmo tempo, o Event Storming mostrou contextos bem separados (atendimento e OS, cadastro de cliente e veículo, estoque, catálogo de serviços) e o grupo trabalha em paralelo, cada um em um pedaço do domínio.

## Decisão

Montamos um único projeto Spring Boot dividido em módulos por contexto. Cada módulo tem três camadas. A `domain` tem entidades, objetos de valor, regras e interfaces de repositório, sem Spring nem JPA. A `application` tem os casos de uso (`*UseCase`), os DTOs e o `@Transactional`. A `infrastructure` tem controllers, entidades JPA, adaptadores e configuração.

## Atualizado em 28/10/2026

O que entrou depois do setup inicial:

- Os módulos hoje são `ordemservico`, `cliente`, `veiculo`, `estoque`, `servico`, `autenticacao` e `compartilhado`. O `compartilhado` só tem utilitários técnicos (paginação, exceções, eventos e padrão de erros).
- O `ordemservico` acessa cliente e veículo por portas próprias (`VerificadorCliente`, `VerificadorVeiculo`, `ConsultorClientes`, `ConsultorVeiculos`).
- Os avisos ao cliente saem de eventos de domínio (`OrdemServicoCriada`, `OrcamentoGerado` etc.), tratados por listeners com `@TransactionalEventListener(AFTER_COMMIT)`.

## Alternativas consideradas

- Microsserviços: separação maior, mas exige mais de um deploy, comunicação pela rede e consistência entre bancos. Não faz sentido para um MVP, e o enunciado pede monólito.
- Camadas globais (`controller/`, `service/`, `repository/` na raiz): mais simples no começo, mas mistura os contextos e gera conflito de merge entre os PRs.
- Arquitetura hexagonal completa, com portas para tudo: mais isolamento, mas muito código de adaptador para o tamanho do projeto.

## Consequências

Positivas:

- Cada contexto fica numa pasta. A divisão ajuda no trabalho em paralelo, mas OS e orçamento exigiram integração entre módulos.
- Um único deploy, um único banco e transações locais (ver [ADR 0004](0004-baixa-de-estoque-na-transacao-do-orcamento.md)).

Negativas:

- O `ordemservico` usa direto os repositórios de domínio de `estoque` e `servico`, e alguns adaptadores usam repositórios JPA de outro módulo, então o isolamento entre módulos depende de disciplina.
- Fica para a próxima fase: os testes ArchUnit hoje só conferem o padrão de erros, não as dependências entre camadas e módulos.
