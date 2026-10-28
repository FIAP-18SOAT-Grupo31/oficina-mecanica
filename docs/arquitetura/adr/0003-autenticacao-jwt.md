# ADR 0003: Autenticação com JWT e código de acesso para o cliente

- **Data:** 07/10/2026
- **Status:** Aceita
- **Responsável:** Henrique Melo

## Contexto

O enunciado pede JWT nas APIs administrativas. Quem usa o sistema é a equipe da oficina. O cliente não tem login, mas precisa acompanhar a OS e aprovar ou rejeitar o orçamento pela API.

## Decisão

A equipe faz login em `POST /api/auth/login`, e o sistema guarda a senha em BCrypt. A resposta traz um JWT HS256 com validade de 1 hora (`app.seguranca.jwt.validade=1h`), emissor `oficina-mecanica` e os papéis na claim `roles`. A chave vem de `JWT_SECRET`, e a aplicação se recusa a subir se ela tiver menos de 32 caracteres.

A API valida o token com o Resource Server do Spring Security (`SegurancaConfig`). Não guardamos sessão (`STATELESS`) e desligamos `httpBasic` e `formLogin`. Existem dois papéis, `ADMIN` e `ATENDENTE`. Os endpoints de negócio aceitam os dois via `@PreAuthorize`, e o Actuator, fora `health` e `prometheus`, só aceita `ADMIN`. Removemos o papel `MECANICO` na migration `V6`.

Desabilitamos o CSRF porque o token vai no cabeçalho `Authorization`, não em cookie.

Para o cliente, o sistema gera um código de acesso de 6 caracteres ao abrir a OS. Com o CPF/CNPJ e esse código, o cliente usa as rotas `/api/publico/**` sem login, e o `VerificadorAcessoClienteAdapter` confere os dois. O `LimiteRequisicoesFilter` limita as requisições por IP e por minuto: um contador para o login e outro para as rotas `/api/publico/**` (padrão 30, `LIMITE_REQUISICOES_POR_MINUTO`).

## Alternativas consideradas

- Sessão com cookie: escolhemos Bearer JWT para a API não precisar guardar sessão no servidor.
- Servidor de identidade externo (Keycloak, por exemplo): mais um container e mais configuração para dois papéis.
- Login para o cliente: mais cadastro e senha para quem só quer ver uma OS. O código de acesso por OS resolve com menos atrito.

## Consequências

Positivas:

- Não precisamos guardar sessão, e qualquer instância valida o token só com a chave.
- O cliente acessa só a OS cujo código recebeu.

Negativas:

- Não existe refresh token nem revogação: um token vazado vale até expirar.
- O limite de requisições fica em memória e vale por instância.
- Ainda falta tirar o código de acesso do log do e-mail simulado.
