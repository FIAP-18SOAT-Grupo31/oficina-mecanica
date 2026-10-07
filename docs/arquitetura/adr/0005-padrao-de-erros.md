# ADR 0005: Padrão único de erros da API

Data: 07/10/2026
Status: aceita

## Contexto

Cada parte do código tratava erro de um jeito: um handler global com `ErroResposta`, `@ExceptionHandler` com `ProblemDetail` dentro de controller, filtro escrevendo JSON na mão e caso de uso lançando a `ValidationException` do framework de validação. O cliente da API recebia formatos diferentes para o mesmo tipo de problema, e erro de validação de campo voltava 500.

## Decisão

Todo erro HTTP sai pelo `TratadorGlobalDeExcecoes`, em `compartilhado/infrastructure/web`, com um único corpo, o `ErroResposta(status, erro, mensagem, timestamp)`, em português.

- As exceções de negócio ficam nas camadas internas e não dependem de framework: `RecursoNaoEncontradoException` (404) e `RegraNegocioException` (422) em `compartilhado/domain/exception`, e `NaoAutenticadoException` (401) em `compartilhado/application/exception`.
- O `TipoErro` concentra o status e o título de cada tipo de erro.
- Filtros e Spring Security, que rodam antes do tratador, escrevem a resposta pelo `EscritorErroResposta`, no mesmo formato.
- Os mapeamentos que já existiam continuam: `IllegalArgumentException` vira 404, `IllegalStateException` vira 422 e qualquer outro erro vira 500, sem expor a mensagem interna.

Um teste de arquitetura (ArchUnit) reprova o build se houver outro `@ControllerAdvice`, `@ExceptionHandler` fora do tratador, uso de `ProblemDetail` ou da `ValidationException` do jakarta.

## Alternativas

- `ProblemDetail` (RFC 9457): é padrão de mercado, mas o grupo já usava o `ErroResposta` e trocar o formato quebraria quem consome a API.
- Tratamento por controller: repete código e deixa cada contexto com um formato.

## Consequências

Quem consome a API trata erro de um jeito só. Quem escreve código só lança a exceção certa e não precisa tratar nada no controller. A regra é conferida pelo build, então não depende de revisão manual.
