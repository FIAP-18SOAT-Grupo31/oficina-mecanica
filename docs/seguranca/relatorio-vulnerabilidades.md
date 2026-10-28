# Relatório de análise de vulnerabilidades

| Item | Valor |
|---|---|
| Ferramenta | Trivy, via `aquasecurity/trivy-action@v0.36.0` no GitHub Actions (`trivy fs --scanners vuln,secret,misconfig`) |
| Execução de referência | CI da branch `dev` no commit `079272e` em 28/10/2026 ([run 40682335666](https://github.com/FIAP-18SOAT-Grupo31/oficina-mecanica/actions/runs/40682335666)). Será atualizada para o commit final da entrega. |
| Escopo | dependências Maven (`pom.xml`), `Dockerfile` e segredos no código |
| Fora do escopo | `target/` e o `.env` local, que não vai para o git |
| Resultado | 0 vulnerabilidades nas dependências, 0 problemas de configuração no `Dockerfile` e nenhum segredo encontrado ([saída completa](trivy-resultado.txt); ver a nota sobre segredos abaixo) |

O resultado acima é o artefato `relatorio-vulnerabilidades` publicado por essa execução do CI, copiado sem alteração para [`trivy-resultado.txt`](trivy-resultado.txt).

Na tabela do Trivy a coluna `Secrets` aparece como `-` nas linhas do `pom.xml` e do `Dockerfile` porque essas linhas são dos scanners de dependência e de configuração. O scanner de segredos só gera linha quando acha algo, e não achou. O passo de bloqueio (`exit-code: 1`) também passou.

## O que é verificado

O job **Vulnerabilidades, segredos e Dockerfile** (`.github/workflows/ci.yml`) roda em todo PR e em todo push para `dev` e `main`:

1. Baixa as dependências para o cache do Maven (`mvnw dependency:resolve`).
2. Gera o relatório completo com severidade CRITICAL, HIGH e MEDIUM para dependências (`vuln`), segredos no repositório (`secret`) e configuração do `Dockerfile` (`misconfig`).
3. Roda o Trivy de novo como bloqueio: o job falha se houver vulnerabilidade CRITICAL ou HIGH que já tenha correção disponível.
4. Publica o relatório completo como artefato do workflow.

Como `main` e `dev` só recebem PR com os checks obrigatórios, o merge fica bloqueado se aparecer CVE crítica ou alta com correção.

## O que já apareceu e como foi tratado

Achados da análise de 07/10/2026, corrigidos antes da versão atual:

| Severidade | Achado | Onde | Tratamento |
|---|---|---|---|
| CRITICAL | CVE-2026-65182, CVE-2026-65905, CVE-2026-68525 | `tomcat-embed-core` 11.0.24, trazido pelo Spring Boot | `tomcat.version` fixada em 11.0.25 no `pom.xml` |
| HIGH | CVE-2026-89407, CVE-2026-89425, CVE-2026-68497, CVE-2026-91776, CVE-2026-91777 | `jackson-core` e `jackson-databind` 2.21.5 e 3.1.5, trazidos pelo Spring Boot | `jackson-2-bom.version` 2.21.7 e `jackson-bom.version` 3.1.7 no `pom.xml` |
| MEDIUM | CVE-2026-19032, CVE-2026-83557 | `jackson-databind` | resolvido pela mesma atualização |
| HIGH | DS-0002: container rodando como root | `Dockerfile` | imagem final roda com o usuário `oficina`, sem privilégio de root |
| LOW | imagem sem `HEALTHCHECK` | `Dockerfile` | `HEALTHCHECK` na porta 8080 |

Não ignoramos nenhum achado: resolvemos as CVEs subindo a versão.

## Outras análises

O CodeQL é a análise estática (SAST) do projeto. Ele roda pela configuração padrão do code scanning do GitHub, por isso não tem arquivo em `.github/workflows`, e analisa o código Java e os workflows em todo PR (checks `Analyze (java-kotlin)` e `Analyze (actions)`). Apareceu um único alerta, `java/spring-disabled-csrf-protection`, que fechamos como "won't fix": a API é stateless, não usa sessão nem cookie e o token vai no cabeçalho `Authorization`, então CSRF não se aplica (ver [ADR 0003](../arquitetura/adr/0003-autenticacao-jwt.md)).

O Spotless padroniza os imports nos arquivos alterados em cada PR.

## Pontos de atenção conhecidos

O e-mail simulado (`NotificadorOrdemServicoAdapter` e `NotificadorEmailAdapter`) grava no log, em nível INFO, a mensagem com o código de acesso da OS. Junto com o CPF/CNPJ, esse código dá acesso à área pública do cliente. Combinamos de mascarar o código e trocar a URL fixa `http://localhost:8080` por uma propriedade no #42; na última revisão deste relatório isso ainda não tinha sido feito.

## Controles da aplicação

| Controle | Como está no código |
|---|---|
| Autenticação | JWT HS256 com validade de 1 hora e emissor validado. A chave vem de `JWT_SECRET` e a aplicação não sobe se ela tiver menos de 32 caracteres (`PropriedadesJwt`). |
| Papéis | `ADMIN` e `ATENDENTE`, conferidos com `@PreAuthorize` em cada endpoint. O Actuator, fora as rotas abertas abaixo, exige `ADMIN`. |
| Rotas abertas (`SegurancaConfig`) | `/api/auth/**`, `/api/publico/**`, `/`, `/index.html`, `/logo.svg`, `/error`, `/swagger-ui.html`, `/swagger-ui/**`, `/v3/api-docs/**`, `/actuator/health/**` e `/actuator/prometheus`. Nas rotas `/api/publico/**` o cliente se identifica com CPF/CNPJ e o código de acesso da OS. |
| Limite de requisições | `LimiteRequisicoesFilter`: por minuto e por IP em `/api/auth/login` e `/api/publico/**` (padrão 30). |
| Senhas | BCrypt. Os usuários de demonstração só são criados quando `DEMO_PASSWORD` está definida. |
| Erros | Respostas no formato `ErroResposta`, sem stack trace e sem mensagem interna. |
| Dados de entrada | Bean Validation nos DTOs; CPF/CNPJ validado pelos dígitos verificadores e placa no padrão antigo ou Mercosul. |
| Segredos | O `.env` está no `.gitignore` e o `.env.example` só traz valores de exemplo. |

## OWASP Top 10

Listamos só os itens em que existe um controle no código ou no pipeline.

| Item (OWASP 2021) | O que fizemos |
|---|---|
| A01 Quebra de controle de acesso | Papéis `ADMIN` e `ATENDENTE` com `@PreAuthorize`; Actuator só para `ADMIN`; área pública exige CPF/CNPJ e código de acesso da OS (`VerificadorAcessoClienteAdapter`). |
| A02 Falhas criptográficas | Senhas com BCrypt; JWT assinado com HS256 e chave de 32 caracteres ou mais vinda de variável de ambiente. |
| A03 Injeção | Acesso ao banco por Spring Data JPA (consultas derivadas e Specifications) e um único SQL manual, com parâmetro `?`, em `VerificadorUsoDoServicoAdapter`. Nenhum SQL é montado concatenando texto. Bean Validation nos DTOs de entrada. |
| A05 Configuração incorreta | Sessão `STATELESS`, `httpBasic` e `formLogin` desligados, Actuator exposto só com `health`, `info` e `metrics` por padrão e `show-details=never`, container rodando sem root. |
| A06 Componentes vulneráveis | Trivy em todo PR bloqueando CVE crítica ou alta com correção (seções acima). |
| A07 Falhas de autenticação | Token com expiração e emissor validados, limite de tentativas por IP no login, usuários de demonstração só com senha definida. |

Os itens A04, A08, A09 e A10 não têm um controle específico no código. Falha de login, por exemplo, não é registrada em log.

## Como reproduzir

Pelo GitHub: abra a execução do workflow **CI** em qualquer PR ou na `dev` e baixe o artefato `relatorio-vulnerabilidades`.

Localmente, com Docker:

```bash
./mvnw -B -q dependency:resolve
docker run --rm -v "$PWD":/src -v "$HOME/.m2":/root/.m2:ro -w /src aquasec/trivy:0.75.0 \
  fs --scanners vuln,secret,misconfig --severity CRITICAL,HIGH,MEDIUM --skip-dirs target .
```

O primeiro comando baixa as dependências para o cache local do Maven. Assim o Trivy não consulta o Maven Central, que devolve erro 429 quando recebe muitas consultas seguidas.
