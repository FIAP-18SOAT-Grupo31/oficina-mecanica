# ADR 0001: Monólito modular com Clean Architecture

Data: 06/10/2026
Status: aceita

## Contexto

O desafio pede um MVP com back-end monolítico. O domínio tem partes bem separadas (clientes, veículos, catálogo, estoque, ordem de serviço), e o grupo tem cinco pessoas trabalhando ao mesmo tempo.

## Decisão

Um único projeto Spring Boot, dividido em pacotes pelos contextos do Event Storming. Cada contexto segue a Clean Architecture, com portas e adaptadores:

- `domain` guarda as regras e declara interfaces para o que precisa de fora, como repositórios;
- `application` tem os casos de uso;
- `infrastructure` implementa essas interfaces e expõe a API REST.

O domínio não importa nada do Spring nem do JPA.

## Alternativas

- Microsserviços: deploy, rede e dados distribuídos demais para um MVP.
- Camadas globais (um pacote `controller`, um `service`, um `repository` para tudo): mais simples no início, mas mistura os contextos e gera conflito entre quem trabalha em partes diferentes.
- Vertical Slice: organiza por funcionalidade, mas deixa as regras do domínio espalhadas, e o desafio pede DDD.

## Consequências

Um deploy só e um banco só. As regras de negócio são testadas sem banco e sem servidor. Cada pessoa trabalha no seu contexto com pouco conflito de merge. Em troca, há mais classes por funcionalidade (entidade de domínio, entidade JPA e mapper).
