# Relatório de análise de vulnerabilidades

| Item | Valor |
|---|---|
| Ferramenta | Trivy, via `aquasecurity/trivy-action@v0.36.0` no GitHub Actions (`trivy fs --scanners vuln,secret,misconfig`) |
| Execução de referência | CI da branch `dev` no commit `079272e` em 28/10/2026 ([run 40682335666](https://github.com/FIAP-18SOAT-Grupo31/oficina-mecanica/actions/runs/40682335666)) |
| Escopo | dependências Maven (`pom.xml`), `Dockerfile` e segredos no código |
| Fora do escopo | `target/` e o `.env` local, que não vai para o git |
| Resultado | 0 vulnerabilidades nas dependências e 0 problemas de configuração no `Dockerfile` ([saída completa](trivy-resultado.txt)). O scanner de segredos está ligado no CI (`scanners: vuln,secret,misconfig`), mas na tabela do anexo a coluna `Secrets` está como `-` (não verificado) nas duas linhas, então não dá para tirar conclusão sobre segredos só por ela |

A saída é o artefato `relatorio-vulnerabilidades` dessa execução, copiado para [`trivy-resultado.txt`](trivy-resultado.txt).

## O que é verificado

O job **Vulnerabilidades, segredos e Dockerfile** (`.github/workflows/ci.yml`) roda em todo PR e em todo push para `dev` e `main`. Ele gera o relatório com severidade CRITICAL, HIGH e MEDIUM e falha se houver CVE CRITICAL ou HIGH com correção disponível.

## O que já apareceu e como foi tratado

Achados da análise de 07/10/2026:

| Severidade | Achado | Onde | Tratamento |
|---|---|---|---|
| CRITICAL | CVE-2026-65182, CVE-2026-65905, CVE-2026-68525 | `tomcat-embed-core` 11.0.24, trazido pelo Spring Boot | `tomcat.version` fixada em 11.0.25 no `pom.xml` |
| HIGH | CVE-2026-89407, CVE-2026-89425, CVE-2026-68497, CVE-2026-91776, CVE-2026-91777 | `jackson-core` e `jackson-databind` 2.21.5 e 3.1.5, trazidos pelo Spring Boot | `jackson-2-bom.version` 2.21.7 e `jackson-bom.version` 3.1.7 no `pom.xml` |
| MEDIUM | CVE-2026-19032, CVE-2026-83557 | `jackson-databind` | resolvido pela mesma atualização |
| HIGH | DS-0002: container rodando como root | `Dockerfile` | imagem final roda com o usuário `oficina`, sem privilégio de root |
| LOW | imagem sem `HEALTHCHECK` | `Dockerfile` | `HEALTHCHECK` na porta 8080 |

Atualizamos as dependências e corrigimos a configuração do Dockerfile para resolver os achados do Trivy listados acima.

## Outras análises

O CodeQL (configuração padrão do code scanning do GitHub) analisa o código Java e os workflows em todo PR. O alerta `java/spring-disabled-csrf-protection` foi fechado como won't fix, porque o token vai no cabeçalho `Authorization` e não em cookie (ver [ADR 0003](../arquitetura/adr/0003-autenticacao-jwt.md)).

## Pontos de atenção conhecidos

O e-mail simulado (`NotificadorOrdemServicoAdapter` e `NotificadorEmailAdapter`) grava no log, em nível INFO, a mensagem com o código de acesso da OS. Junto com o CPF/CNPJ, esse código dá acesso à área pública do cliente. Mascarar o código e trocar a URL fixa `http://localhost:8080` por uma propriedade ficou para o #42.

## Controles da aplicação

| Controle | Como está no código |
|---|---|
| Autenticação | JWT HS256 com validade de 1 hora e emissor validado. A chave vem de `JWT_SECRET` e a aplicação não sobe se ela tiver menos de 32 caracteres (`PropriedadesJwt`). |
| Papéis | `ADMIN` e `ATENDENTE`, conferidos com `@PreAuthorize` em cada endpoint. O Actuator, fora as rotas abertas abaixo, exige `ADMIN`. |
| Rotas abertas (`SegurancaConfig`) | `/api/auth/**`, `/api/publico/**`, `/`, `/index.html`, `/logo.svg`, `/error`, `/swagger-ui.html`, `/swagger-ui/**`, `/v3/api-docs/**`, `/actuator/health/**` e `/actuator/prometheus`. Nas rotas `/api/publico/**` o cliente se identifica com CPF/CNPJ e o código de acesso da OS. |
| Limite de requisições | `LimiteRequisicoesFilter`: 30 por minuto por IP (padrão), um contador para o login e outro para `/api/publico/**`. |
| Senhas | BCrypt. Os usuários de demonstração só são criados quando `DEMO_PASSWORD` está definida. |
| Erros | Respostas no formato `ErroResposta`, sem stack trace e sem mensagem interna. |
| Dados de entrada | Bean Validation nos DTOs; CPF/CNPJ validado pelos dígitos verificadores e placa no padrão antigo ou Mercosul. |
| Segredos | O `.env` está no `.gitignore` e o `.env.example` só traz valores de exemplo. |

## OWASP Top 10

| Item (OWASP 2021) | O que fizemos |
|---|---|
| A01 Quebra de controle de acesso | Papéis `ADMIN` e `ATENDENTE` com `@PreAuthorize`; Actuator só para `ADMIN`; área pública exige CPF/CNPJ e código de acesso da OS (`VerificadorAcessoClienteAdapter`). |
| A02 Falhas criptográficas | Senhas com BCrypt; JWT assinado com HS256 e chave de 32 caracteres ou mais vinda de variável de ambiente. |
| A03 Injeção | Acesso ao banco por Spring Data JPA (consultas derivadas e Specifications) e um único SQL manual, com parâmetro `?`, em `VerificadorUsoDoServicoAdapter`. Nenhum SQL é montado concatenando texto. Bean Validation nos DTOs de entrada. |
| A05 Configuração incorreta | Sessão `STATELESS`, `httpBasic` e `formLogin` desligados, Actuator exposto só com `health`, `info` e `metrics` por padrão e `show-details=never`, container rodando sem root. |
| A06 Componentes vulneráveis | Trivy em todo PR bloqueando CVE crítica ou alta com correção (seções acima). |
| A07 Falhas de autenticação | Token com expiração e emissor validados, limite de tentativas por IP no login, usuários de demonstração só com senha definida. |

A04, A08, A09 e A10 não têm controle específico no código.

## Como reproduzir

Pelo GitHub: abra a execução do workflow **CI** em qualquer PR ou na `dev` e baixe o artefato `relatorio-vulnerabilidades`.

Localmente, com Docker:

```bash
./mvnw -B -q dependency:resolve
docker run --rm -v "$PWD":/src -v "$HOME/.m2":/root/.m2:ro -w /src aquasec/trivy:0.75.0 \
  fs --scanners vuln,secret,misconfig --severity CRITICAL,HIGH,MEDIUM --skip-dirs target .
```

Baixar as dependências antes evita que o Trivy consulte o Maven Central e tome 429.
