# ADR 0004: Reserva e baixa de estoque na mesma transação do orçamento

- **Data:** 13/10/2026
- **Status:** Aceita
- **Responsável:** Ruan Pablo (commits `ef972e2` e `05a6f8e`)

## Contexto

O orçamento usa peças do estoque. Se a peça só fosse conferida na aprovação, o cliente poderia aprovar um orçamento com peça que já foi usada em outra OS. Também não pode acontecer de o orçamento ficar aprovado e o estoque não baixar, ou o contrário.

## Decisão

Dividimos o saldo de cada peça em duas partes no `ProdutoEstoque`: `quantidadeDisponivel` e `quantidadeReservada`. Cada passo do orçamento mexe no estoque dentro do próprio caso de uso, numa única transação (`@Transactional`).

O `GerarOrcamentoUseCase` chama `reservar` para cada peça, movendo a quantidade do disponível para o reservado. Se faltar saldo, ele lança `RegraNegocioException` e nada é gravado.

O `AprovarOrcamentoUseCase` chama `confirmarBaixa` para as peças aprovadas (que saem do reservado) e `cancelarReserva` para as recusadas na aprovação parcial (que voltam ao disponível), junto com a mudança de status do orçamento e da OS. A versão do cliente, `AprovarOrcamentoClienteUseCase`, confere o acesso e delega para ele. Do mesmo jeito, o `RejeitarOrcamentoUseCase` (também chamado pelo `RejeitarOrcamentoClienteUseCase`) e o `CancelarOrdemServicoUseCase` devolvem a reserva de todas as peças com `cancelarReserva`.

Quando o cliente tenta aprovar um orçamento vencido, a aprovação usa `noRollbackFor = RegraNegocioException.class`: o orçamento fica `EXPIRADO`, a reserva volta e a OS fica Recusada, mesmo com a resposta de erro.

Os casos de uso do `ordemservico` usam direto o `ProdutoEstoqueRepository` do módulo `estoque`. Os eventos de domínio servem só para notificar o cliente e rodam depois do commit.

## Alternativas consideradas

- Baixar o estoque só na aprovação, sem reserva: mais simples, mas permite prometer a mesma peça em dois orçamentos.
- Estoque reagindo a eventos (`OrdemServicoAprovada`) depois do commit: desacopla os módulos, mas a OS pode ficar aprovada sem a baixa se o listener falhar, e precisaria de compensação.
- Porta própria no `ordemservico` para o estoque, como fizemos com cliente e veículo: menos acoplamento, mais código. Ficou para depois.

## Consequências

Positivas:

- Orçamento, OS e estoque mudam juntos ou não mudam.
- O atendente vê no estoque quanto está livre e quanto está preso em orçamentos pendentes.

Negativas:

- O `ordemservico` depende direto do repositório de `estoque`.
- Ponto em aberto: não há lock otimista (`@Version`) nem pessimista em `produtos_estoque`. Dois orçamentos gerados ao mesmo tempo para a mesma peça podem ler o mesmo saldo e reservar mais do que existe.
