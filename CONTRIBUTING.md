# Como contribuir

## Branches

| Branch | Para quê |
|---|---|
| `main` | Versão entregue para avaliação. Só recebe PR vindo da `dev`. |
| `dev` | Integração do grupo. Todo trabalho novo entra primeiro aqui. |
| `feat/<assunto>` | Funcionalidade nova, criada a partir da `dev`. |
| `fix/<assunto>` | Correção, criada a partir da `dev`. |
| `docs/`, `test/`, `refactor/`, `chore/`, `ci/` | Documentação, testes, refatoração, manutenção e CI, também a partir da `dev`. |

Ninguém faz push direto na `main` nem na `dev`. Tudo entra por pull request.

## Fluxo

```bash
git switch dev
git pull
git switch -c feat/cadastro-cliente
# ... código e testes ...
./mvnw spotless:apply
./mvnw verify
git push -u origin feat/cadastro-cliente
```

Depois, abrir o PR para a `dev`. Quando a `dev` estiver estável para uma entrega, abre-se um PR da `dev` para a `main`.

## Nomes

Branch no formato `tipo/assunto`, em minúsculas e com hífen. Título do PR e commits no formato `tipo: descrição`, em português e em minúsculas. Tipos aceitos: `feat`, `fix`, `docs`, `test`, `refactor`, `chore`, `ci`, `style` e `perf`.

```
feat: cadastro de cliente com validação de CPF
fix: status da OS não voltava para diagnóstico
test: casos de aprovação de orçamento
docs: instruções de execução local
```

## Regras de merge

| | `dev` | `main` |
|---|---|---|
| Aprovação | 1, descartada a cada push novo | 1, descartada a cada push novo |
| Conversas do PR | precisam estar resolvidas | |
| Checks obrigatórios | build e cobertura, imports, Trivy e nomes | os mesmos, mais a origem do PR |
| Branch atualizada antes do merge | sim | sim |
| Tipo de merge | squash (vira um commit com o título do PR) | merge commit |

A branch do PR é apagada sozinha depois do merge.

Se um PR depende de outro que ainda não entrou, ele mostra também os commits do anterior. Depois que o anterior entrar, rebaseie: `git rebase --onto origin/dev <último commit do PR anterior> <sua branch>`.

## Padrões de código

- Nomes em português, no padrão do projeto: `*UseCase` com o método `execute`, `*Input` e `*Output`, `*Entity`, `*Mapper` e `*RepositoryAdapter`.
- Indentação de 4 espaços.
- Imports sem `*`, sem import sem uso e na ordem do IntelliJ (outros pacotes, depois `java` e `javax`, depois `static`). `./mvnw spotless:apply` corrige a ordem e remove os que não são usados. O `*` precisa ser trocado na mão.
- Erros: não encontrado é `RecursoNaoEncontradoException`, regra de negócio violada é `RegraNegocioException`. Não use `@ExceptionHandler` em controller nem `ProblemDetail`; o `TratadorGlobalDeExcecoes` cuida da resposta. O build reprova se isso for quebrado.
- Validação de entrada com Bean Validation (`@NotBlank`, `@Size` e outras) no `*Input` e `@Valid` no controller.

## Antes de abrir o PR

- `./mvnw spotless:apply` e `./mvnw verify` passando. O build falha se a cobertura de linhas do projeto ficar abaixo de 80%.
- Mudou entidade ou coluna? Crie uma migration nova em `src/main/resources/db/migration` (`V6__descricao.sql`, `V7__...`). Migration que já foi para a `dev` não se altera.
- Dado só para testar localmente vai em `src/main/resources/db/seed/dev`, que não roda em produção.
- Endpoint novo precisa aparecer no Swagger.
