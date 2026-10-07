# Linguagem Ubíqua da Oficina Mecânica (Grupo 31)

Vocabulário compartilhado entre o negócio, as Domain Stories (egon.io), o Event Storming (Miro) e o código.

## Decisão central: Status da OS × Situação do Orçamento

A Ordem de Serviço tem exatamente os seis status do enunciado. A aprovação/reprovação é um fato do
Orçamento, com ciclo de vida próprio. Isso evita um status extra ("Aprovada") e separa OS reprovadas
das executadas no cálculo do tempo médio.

| Conceito | Valores |
|---|---|
| Status da OS | Recebida → Em diagnóstico → Aguardando aprovação → Em execução → Finalizada → Entregue |
| Situação do Orçamento | Em elaboração → Enviado → Aprovado / Aprovado parcialmente / Reprovado |

## Glossário

| Termo | Definição |
|---|---|
| Ordem de Serviço (OS) | Registro do atendimento de um veículo, da recepção à entrega. Agregado principal do sistema. |
| Código de acompanhamento | Identificador curto, aleatório e não sequencial enviado ao cliente para consultar e aprovar a OS. |
| Status da OS | Etapa do atendimento; muda automaticamente conforme as ações e só por transições permitidas. |
| Recebida | OS aberta pelo atendente com cliente e veículo identificados. |
| Em diagnóstico | OS aberta sem serviços; o mecânico analisa o veículo para identificá-los. |
| Aguardando aprovação | Orçamento enviado ao cliente, aguardando decisão. |
| Em execução | Orçamento aprovado (total ou parcialmente); o mecânico executa os serviços. |
| Finalizada | Serviços concluídos ou orçamento reprovado; veículo pronto para retirada. |
| Entregue | Cliente retirou o veículo. Estado final. |
| Orçamento | Valor da OS: mão de obra dos serviços + peças/insumos. Pertence à OS (não é agregado separado). |
| Situação do Orçamento | Em elaboração, Enviado, Aprovado, Aprovado parcialmente ou Reprovado. |
| Enviar orçamento | Atendente revisa e envia; a OS passa a Aguardando aprovação e o orçamento fica bloqueado para edição. |
| Aprovação total | Cliente aceita todo o orçamento. OS vai para Em execução e o estoque é baixado na mesma transação. |
| Aprovação parcial | Cliente aceita só parte dos serviços e/ou recusa peças. Itens recusados saem da OS e o orçamento é recalculado. |
| Reprovação | Cliente recusa o orçamento. OS vai para Finalizada sem execução e sem movimentar estoque. |
| Diagnóstico | Análise registrada pelo mecânico que justifica os serviços incluídos. (Substitui o antigo "Relatório" do Miro.) |
| Serviço | Item do catálogo (ex.: troca de óleo) com preço de mão de obra e peças/insumos padrão. |
| Peça padrão | Peça/insumo e quantidade que um serviço normalmente consome; é sugerida ao incluir o serviço na OS. |
| Item de serviço / Item de peça | Cópia do serviço/peça dentro da OS com o preço vigente no momento da inclusão. |
| Peça | Item físico unitário (ex.: filtro, pastilha). |
| Insumo | Item consumível (ex.: óleo, fluido). |
| Estoque / saldo | Quantidade disponível de uma peça/insumo. Só muda por entrada ou baixa. |
| Entrada de estoque | Reposição registrada pelo administrador. |
| Baixa de estoque | Redução do saldo ao aprovar o orçamento, apenas dos itens aprovados. |
| Estoque insuficiente | Saldo menor que o necessário. Na geração do orçamento vira observação; na aprovação impede a aprovação (rollback). |
| Estoque mínimo | Limite abaixo do qual a peça é sinalizada para reposição. |
| Histórico de status | Registro de cada transição com data/hora. Base do acompanhamento e das métricas. |
| Tempo de execução | Intervalo entre o início e o fim da execução registrados pelo mecânico. |
| Tempo médio de execução | Média do tempo de execução das OS com orçamento aprovado; reprovadas não entram. |
| Link de acompanhamento / Notificação | Aviso ao cliente na abertura da OS e a cada mudança de status. No MVP é registrado em log. |
| Cliente | Pessoa física ou jurídica identificada por CPF/CNPJ. Não tem login: usa a API pública. |
| CPF/CNPJ (Documento) | Objeto de valor com validação dos dígitos verificadores. |
| Veículo | Automóvel do cliente, identificado pela placa. |
| Placa | Objeto de valor no formato antigo (ABC1234) ou Mercosul (ABC1D23). |
| Atendente | Abre a OS, ajusta e envia orçamento, registra aprovação recebida fora do sistema e a entrega. |
| Mecânico | Registra diagnóstico, inicia e finaliza a execução. |
| Administrador | Gerencia catálogo, estoque e consulta métricas; tem todas as permissões. |
| Sistema | Ator automático: valida dados, sugere peças, verifica/baixa estoque, muda status e notifica. |

## Termos evitados (e o que usar no lugar)

| Evitar | Usar | Motivo |
|---|---|---|
| "OS Aprovada" (como status) | Orçamento Aprovado + OS Em execução | O enunciado define seis status fixos. |
| "Relatório" | Diagnóstico | Relatório era genérico; o artefato é o diagnóstico do mecânico. |
| "Ordem de serviço atualizada" (evento) | O fato específico: Orçamento enviado, Execução iniciada… | Evento de domínio deve dizer o que aconteceu. |
| "PDF do orçamento" | Link de acompanhamento / API pública | O cliente consulta e decide pela API. |
