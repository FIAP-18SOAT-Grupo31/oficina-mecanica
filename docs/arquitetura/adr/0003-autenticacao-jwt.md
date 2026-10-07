# ADR 0003: Autenticação com JWT

Data: 06/10/2026
Status: aceita

## Contexto

O desafio exige JWT nas APIs administrativas. A consulta do cliente à própria ordem de serviço, prevista no desafio, vai ficar em rotas abertas sob `/api/publico`, que ainda não foram implementadas.

## Decisão

Login próprio em `POST /api/auth/login`, que devolve um JWT assinado com HS256, válido por uma hora, com o papel do usuário (ADMIN, ATENDENTE ou MECANICO). A validação usa o Spring Security OAuth2 Resource Server. A chave vem de variável de ambiente e tem no mínimo 32 bytes. Senhas guardadas com BCrypt.

## Alternativas

- Provedor externo (Keycloak, Cognito): mais um serviço para subir e configurar, sem ganho para o MVP.
- Sessão no servidor: não combina com uma API sem estado.

## Consequências

A API não guarda sessão. Não há como revogar um token antes de vencer, por isso a validade curta. Login e rotas públicas têm limite de requisições por IP.

Como o token vai no cabeçalho `Authorization` e não existe cookie de sessão, a proteção contra CSRF do Spring fica desligada: o navegador não envia esse cabeçalho sozinho, então o ataque não se aplica. O alerta do CodeQL sobre isso foi fechado com essa justificativa.

Erros de autenticação (401), de permissão (403) e de excesso de tentativas (429) saem no mesmo formato dos outros erros da API (ADR 0005).
