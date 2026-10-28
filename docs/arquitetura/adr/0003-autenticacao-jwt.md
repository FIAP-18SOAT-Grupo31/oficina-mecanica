# ADR 0003: Autenticação com JWT e código de acesso para o cliente

Status: aceito (07/10/2026, commit `13ba670`)

## Contexto

O enunciado pede JWT nas APIs administrativas. Quem usa o sistema é a equipe da oficina. O cliente não tem login, mas precisa acompanhar a OS e aprovar ou rejeitar o orçamento pela API.

## Decisão

- Login em `POST /api/auth/login`, com senha guardada em BCrypt. A resposta é um JWT HS256 com validade de 1 hora, emissor `oficina-mecanica` e os papéis na claim `roles`. A chave vem de `JWT_SECRET`, e a aplicação não sobe se ela tiver menos de 32 caracteres.
- A API valida o token com o Resource Server do Spring Security (`SegurancaConfig`). Não há sessão (`STATELESS`), e `httpBasic` e `formLogin` estão desligados.
- Dois papéis: `ADMIN` e `ATENDENTE`. Os endpoints de negócio aceitam os dois (`@PreAuthorize`); o Actuator, fora `health` e `prometheus`, só aceita `ADMIN`. O papel `MECANICO` saiu na migration `V6`.
- CSRF desabilitado. A API não usa cookie nem sessão e o token vai no cabeçalho `Authorization`, então o navegador não manda credencial sozinho e o ataque não se aplica.
- Para o cliente: ao abrir a OS, é gerado um código de acesso de 6 caracteres. Com o CPF/CNPJ e esse código, o cliente usa as rotas `/api/publico/**` sem login. O `VerificadorAcessoClienteAdapter` confere os dois.
- `/api/auth/login` e `/api/publico/**` têm limite de requisições por minuto por IP (`LimiteRequisicoesFilter`, padrão 30).

## Alternativas consideradas

- Sessão com cookie: exigiria CSRF e estado no servidor; não combina com uma API REST consumida por outros clientes.
- Servidor de identidade externo (Keycloak, por exemplo): mais um container e mais configuração para dois papéis.
- Login para o cliente: mais cadastro e senha para quem só quer ver uma OS. O código de acesso por OS resolve com menos atrito.

## Consequências

- Não precisamos guardar sessão, e qualquer instância valida o token só com a chave.
- O cliente acessa só a OS cujo código recebeu.
- Débito técnico: não existe refresh token nem revogação (um token vazado vale até expirar); o limite de requisições fica em memória e vale por instância; o código de acesso ainda aparece no log do e-mail simulado.
