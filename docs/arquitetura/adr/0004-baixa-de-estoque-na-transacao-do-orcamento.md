# ADR 0004: Reserva e baixa de estoque na mesma transação do orçamento

Status: aceito (13/10/2026, commit `ef972e2`)

## Contexto

O orçamento usa peças do estoque. Se a peça só fosse conferida na aprovação, o cliente poderia aprovar um orçamento com peça que já foi usada em outra OS. Também não pode acontecer de o orçamento ficar aprovado e o estoque não baixar, ou o contrário.

## Decisão

O saldo de cada peça tem duas partes em `ProdutoEstoque`: `quantidadeDisponivel` e `quantidadeReservada`. Cada passo do orçamento mexe no estoque dentro do próprio caso de uso, numa única transação (`@Transactional`):

- `GerarOrcamentoUseCase`: chama `reservar` para cada peça (disponível vai para reservado). Se faltar saldo, lança `RegraNegocioException` e nada é gravado.
- `AprovarOrcamentoUseCase` (a versão do cliente, `AprovarOrcamentoClienteUseCase`, confere o acesso e chama este): `confirmarBaixa` das peças aprovadas (sai do reservado) e `cancelarReserva` das recusadas na aprovação parcial (volta ao disponível), junto com a mudança de status do orçamento e da OS.
- `RejeitarOrcamentoUseCase` (também chamado pelo `RejeitarOrcamentoClienteUseCase`) e `CancelarOrdemServicoUseCase`: `cancelarReserva` de todas as peças.
- Orçamento expirado: a aprovação usa `noRollbackFor = RegraNegocioException.class`, então o orçamento fica `EXPIRADO`, a reserva volta e a OS fica Recusada, mesmo com a resposta de erro.

Os casos de uso do `ordemservico` usam direto o `ProdutoEstoqueRepository` do módulo `estoque`. Os eventos de domínio só servem para notificar o cliente e rodam depois do commit.

## Alternativas consideradas

- Baixar o estoque só na aprovação, sem reserva: mais simples, mas permite prometer a mesma peça em dois orçamentos.
- Estoque reagindo a eventos (`OrdemServicoAprovada`) depois do commit: desacopla os módulos, mas a OS pode ficar aprovada sem a baixa se o listener falhar, e precisaria de compensação.
- Porta própria no `ordemservico` para o estoque, como fizemos com cliente e veículo: menos acoplamento, mais código. Ficou para depois.

## Consequências

- Orçamento, OS e estoque mudam juntos ou não mudam.
- O atendente vê no estoque quanto está livre e quanto está preso em orçamentos pendentes.
- Débito técnico: não há lock otimista (`@Version`) nem pessimista em `produtos_estoque`. Dois orçamentos gerados ao mesmo tempo para a mesma peça podem ler o mesmo saldo e reservar mais do que existe.
