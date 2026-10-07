# Relatório de análise de vulnerabilidades

| Item | Valor |
|---|---|
| Ferramenta | Trivy 0.75.0 (`trivy fs --scanners vuln,secret,misconfig`) |
| Data | 07/10/2026 |
| Escopo | dependências Maven, `Dockerfile` e segredos no código |
| Fora do escopo | `target/` e o `.env` local, que não vai para o git |
| Resultado | 0 vulnerabilidades, 0 segredos e 0 problemas de configuração ([saída completa](trivy-resultado.txt)) |

## O que apareceu e como foi tratado

| Severidade | Achado | Onde | Tratamento |
|---|---|---|---|
| CRITICAL | CVE-2026-65182, CVE-2026-65905, CVE-2026-68525 | `tomcat-embed-core` 11.0.24, trazido pelo Spring Boot | `tomcat.version` fixada em 11.0.25 no `pom.xml` |
| HIGH | CVE-2026-89407, CVE-2026-89425, CVE-2026-68497, CVE-2026-91776, CVE-2026-91777 | `jackson-core` e `jackson-databind` 2.21.5 e 3.1.5, trazidos pelo Spring Boot | `jackson-2-bom.version` 2.21.7 e `jackson-bom.version` 3.1.7 no `pom.xml` |
| MEDIUM | CVE-2026-19032, CVE-2026-83557 | `jackson-databind` | resolvido pela mesma atualização |
| HIGH | DS-0002: container rodando como root | `Dockerfile` original | usuário `oficina` sem privilégio de root na imagem final |
| LOW | Imagem sem `HEALTHCHECK` | `Dockerfile` original | health check da porta 8080 |

Os achados de dependência foram conferidos de novo em 07/10/2026: tirando as versões fixadas do `pom.xml`, o Trivy volta a apontar exatamente as CVEs da tabela. Nenhum achado foi ignorado ou suprimido. CVE de dependência é corrigida subindo a versão.

## Outras análises

- CodeQL do GitHub analisa o código Java e os workflows em todo PR. O único alerta até agora foi a proteção contra CSRF desligada, fechado como "won't fix": a API não usa sessão nem cookie e o token vai no cabeçalho `Authorization`, então o ataque não se aplica (ADR 0003).

## Controles da aplicação

- APIs protegidas por JWT HS256. A chave tem no mínimo 32 bytes e vem de variável de ambiente. Papéis: ADMIN, ATENDENTE e MECANICO.
- Ficam abertos só o login, o Swagger e o health. O restante do Actuator exige ADMIN.
- Limite de 30 requisições por minuto por IP no login.
- Senhas com BCrypt. Os usuários de demonstração só são criados quando `DEMO_PASSWORD` está definida.
- Erros no formato `ErroResposta`, sem stack trace e sem mensagem interna.
- O container roda com um usuário sem privilégio de root.

## Como reproduzir

```bash
./mvnw -B -q dependency:resolve
docker run --rm -v "$PWD":/src -v "$HOME/.m2":/root/.m2:ro -w /src aquasec/trivy:0.75.0 \
  fs --scanners vuln,secret,misconfig --severity CRITICAL,HIGH,MEDIUM --skip-dirs target .
```

O primeiro comando baixa as dependências para o cache local do Maven, para o Trivy não consultar o Maven Central, que devolve erro 429 quando recebe muitas consultas seguidas.

No CI, o check "Vulnerabilidades, segredos e Dockerfile" roda em todo PR, falha com vulnerabilidade crítica ou alta que já tem correção e publica o relatório completo como artefato `relatorio-vulnerabilidades`.
