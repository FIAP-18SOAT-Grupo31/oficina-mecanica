# Relatório de análise de vulnerabilidades

| Item | Valor |
|---|---|
| Ferramenta | Trivy, via `aquasecurity/trivy-action@v0.36.0` no GitHub Actions (`trivy fs --scanners vuln,secret,misconfig`) |
| Execução de referência | CI da branch `dev` no commit `079272e` em 28/10/2026 ([run 40682335666](https://github.com/FIAP-18SOAT-Grupo31/oficina-mecanica/actions/runs/40682335666)). Será atualizada para o commit final da entrega. |
| Escopo | dependências Maven (`pom.xml`), `Dockerfile` e segredos no código |
| Fora do escopo | `target/` e o `.env` local, que não vai para o git |
| Resultado | 0 vulnerabilidades nas dependências, 0 problemas de configuração no `Dockerfile` e nenhum segredo encontrado ([saída completa](trivy-resultado.txt); ver a nota sobre segredos abaixo) |

O resultado acima é o artefato `relatorio-vulnerabilidades` publicado por essa execução do CI, copiado sem alteração para [`trivy-resultado.txt`](trivy-resultado.txt).

**Sobre os segredos:** na tabela do Trivy, a coluna `Secrets` aparece como `-` ("não escaneado") nas linhas `pom.xml` e `Dockerfile`. Isso acontece porque essas linhas representam os alvos dos scanners de dependência e de configuração, não os arquivos verificados pelo scanner de segredos. O scanner de segredos percorre todos os arquivos do repositório (exceto `target/`), mas só acrescenta uma linha à tabela quando encontra algum segredo. Por isso, a ausência de linhas de segredo significa que nada foi encontrado. Há também uma evidência indireta: o segundo passo do job roda com `scanners: vuln,secret,misconfig` e `exit-code: 1` e passou, então não houve segredo de severidade crítica ou alta. O limite dessa evidência: o Trivy não lista os arquivos verificados sem achado, então o relatório mostra só que não houve ocorrência, não quais arquivos foram lidos.

## O que é verificado

O job **Vulnerabilidades, segredos e Dockerfile** (`.github/workflows/ci.yml`) roda em todo PR e em todo push para `dev` e `main`:

1. Baixa as dependências para o cache do Maven (`mvnw dependency:resolve`).
2. Gera o relatório completo com severidade CRITICAL, HIGH e MEDIUM para dependências (`vuln`), segredos no repositório (`secret`) e configuração do `Dockerfile` (`misconfig`).
3. Roda o Trivy de novo como bloqueio: o job falha se houver vulnerabilidade CRITICAL ou HIGH que já tenha correção disponível.
4. Publica o relatório completo como artefato do workflow.

Como a `main` e a `dev` só recebem código por PR com os checks obrigatórios, nenhuma mudança com vulnerabilidade crítica ou alta corrigível entra nessas branches.

## O que já apareceu e como foi tratado

Achados da análise de 07/10/2026, corrigidos antes da versão atual:

| Severidade | Achado | Onde | Tratamento |
|---|---|---|---|
| CRITICAL | CVE-2026-65182, CVE-2026-65905, CVE-2026-68525 | `tomcat-embed-core` 11.0.24, trazido pelo Spring Boot | `tomcat.version` fixada em 11.0.25 no `pom.xml` |
| HIGH | CVE-2026-89407, CVE-2026-89425, CVE-2026-68497, CVE-2026-91776, CVE-2026-91777 | `jackson-core` e `jackson-databind` 2.21.5 e 3.1.5, trazidos pelo Spring Boot | `jackson-2-bom.version` 2.21.7 e `jackson-bom.version` 3.1.7 no `pom.xml` |
| MEDIUM | CVE-2026-19032, CVE-2026-83557 | `jackson-databind` | resolvido pela mesma atualização |
| HIGH | DS-0002: container rodando como root | `Dockerfile` | imagem final roda com o usuário `oficina`, sem privilégio de root |
| LOW | imagem sem `HEALTHCHECK` | `Dockerfile` | `HEALTHCHECK` na porta 8080 |

Nenhum achado foi ignorado ou suprimido. CVE de dependência é corrigida subindo a versão.

## Outras análises

- **CodeQL** (GitHub code scanning) analisa o código Java e os workflows em todo PR. Existe um único alerta, `java/spring-disabled-csrf-protection`, fechado como "won't fix". A API é stateless, não usa sessão nem cookie, e o token vai no cabeçalho `Authorization`, então o ataque de CSRF não se aplica.
- **Spotless** padroniza os imports nos arquivos alterados em cada PR.

## Pontos de atenção conhecidos

- **Código de acesso no log:** no envio de e-mail simulado (`NotificadorOrdemServicoAdapter`), a mensagem com o código de acesso da OS é registrada em log no nível INFO. Junto com o CPF/CNPJ, esse código dá acesso à área pública do cliente. O Natan vai mascarar o código e mover a URL fixa `http://localhost:8080` para uma propriedade no #42, antes do merge.

## Controles da aplicação

- **Autenticação:** APIs administrativas protegidas por JWT HS256, com validade de 1 hora e emissor validado. A chave vem da variável `JWT_SECRET` e deve ter 32 caracteres ou mais.
- **Papéis:** `ADMIN` e `ATENDENTE`. O Actuator, fora o health check, exige `ADMIN`.
- **Rotas abertas** (`SegurancaConfig`):
  - `/api/auth/**` e `/api/publico/**`;
  - `/`, `/index.html`, `/logo.svg` e `/error`;
  - o Swagger;
  - `/actuator/health/**` e `/actuator/prometheus`.

  Nas rotas públicas, o cliente se identifica com CPF/CNPJ e o código de acesso da OS.
- **Limite de requisições:** por minuto e por IP no login e nas rotas públicas.
- **Senhas:** guardadas com BCrypt. Os usuários de demonstração só são criados quando `DEMO_PASSWORD` está definida.
- **Erros:** respostas no formato `ErroResposta`, sem stack trace e sem mensagem interna.
- **Dados:** CPF/CNPJ validado pelos dígitos verificadores e placa validada no padrão antigo ou Mercosul.
- **Segredos:** nenhum segredo real no repositório. O `.env` está no `.gitignore`, e o `.env.example` só traz valores de exemplo.

## Como reproduzir

Pelo GitHub: abra a execução do workflow **CI** em qualquer PR ou na `dev` e baixe o artefato `relatorio-vulnerabilidades`.

Localmente, com Docker:

```bash
./mvnw -B -q dependency:resolve
docker run --rm -v "$PWD":/src -v "$HOME/.m2":/root/.m2:ro -w /src aquasec/trivy:0.75.0 \
  fs --scanners vuln,secret,misconfig --severity CRITICAL,HIGH,MEDIUM --skip-dirs target .
```

O primeiro comando baixa as dependências para o cache local do Maven. Assim o Trivy não consulta o Maven Central, que devolve erro 429 quando recebe muitas consultas seguidas.
