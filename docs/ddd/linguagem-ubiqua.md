# Linguagem ubíqua: Oficina Mecânica (grupo 31)

Vocabulário usado pelo grupo no Event Storming (Miro), nas histórias do egon.io e no código. Cada termo aponta onde ele aparece no código: classe, enum, evento ou endpoint.

Fontes:
- Event Storming: https://miro.com/app/board/uXjVHnerRBQ=/?share_link_id=849086151688 (exportado em [`event-storming.pdf`](event-storming.pdf))
- Domain Storytelling: [`egon/`](egon) (renderizado em [`diagramas.pdf`](diagramas.pdf))

## Glossário

### Atendimento e ordem de serviço

| Termo | Significado | No código |
|---|---|---|
| Ordem de Serviço (OS) | Registro do atendimento de um veículo, da recepção à entrega. Agregado principal. | `OrdemServico`, `/api/ordens-servico` |
| Relato do problema | O que o cliente conta na recepção (10 a 500 caracteres). | `OrdemServico.relatoProblema` |
| Criar OS | Abrir a OS para um cliente e um veículo já cadastrados. O cliente é identificado pelo id ou pelo CPF/CNPJ (com ou sem pontuação), e o veículo pelo id ou pela placa. O veículo precisa ser do cliente. | `CriarOrdemServicoUseCase`, `POST /api/ordens-servico` (`clienteId` ou `documentoCliente`; `veiculoId` ou `placaVeiculo`) |
| OS criada | Fato que dispara o aviso ao cliente com o código de acesso. | evento `OrdemServicoCriada` |
| Código de acesso | Código curto e aleatório enviado ao cliente. Junto com o CPF/CNPJ, dá acesso à área pública. | `OrdemServico.codigoAcesso` |
| Status da OS | Etapa do atendimento. Só muda pelas operações do agregado. | enum `StatusOrdemServico` |
| Recebida | OS aberta, ainda sem orçamento. | `RECEBIDA` |
| Em diagnóstico | Oficina identificando serviços e peças. | `EM_DIAGNOSTICO` |
| Aguardando aprovação | Orçamento enviado, esperando a decisão do cliente. | `AGUARDANDO_APROVACAO` |
| Aprovada | Orçamento aprovado (total ou parcial), serviço liberado para execução. | `APROVADA`, evento `OrdemServicoAprovada` |
| Recusada | Orçamento rejeitado ou expirado. Estado final, sem execução. | `RECUSADA`, evento `OrdemServicoRecusada` |
| Em execução | Serviços sendo feitos. | `EM_EXECUCAO`, `PATCH /api/ordens-servico/{id}/iniciar-execucao` |
| Finalizada | Serviços concluídos; o cliente é avisado que o veículo está pronto. | `FINALIZADA`, evento `OrdemServicoFinalizada`, `PATCH .../finalizar` |
| Entregue | Veículo retirado pelo cliente. Estado final. | `ENTREGUE`, `PATCH .../entregar` |
| Cancelada | OS encerrada antes da aprovação; o orçamento pendente é rejeitado e as peças voltam ao estoque. | `CANCELADA`, evento `OrdemServicoCancelada`, `PATCH .../cancelar` |
| Histórico de status | Registro de cada mudança de status com data e hora. | `TransicaoStatus`, tabela `historico_status_ordem_servico` |
| Tempo médio de execução | Média do tempo entre o início da execução e a finalização das OS. | calculado a partir do histórico de status; consulta em desenvolvimento no #42 |
| Listagem / detalhe da OS | Consulta administrativa das OS, com filtro por status. | `GET /api/ordens-servico`, `GET /api/ordens-servico/{id}` |
| Acompanhamento pelo cliente | Consulta do andamento sem login. | `POST /api/publico/ordens-servico/{id}/consultar` |

### Orçamento

| Termo | Significado | No código |
|---|---|---|
| Orçamento | Proposta de valor da OS: mão de obra dos serviços + peças e insumos. | `Orcamento`, `/api/orcamentos` |
| Gerar orçamento | Montar o orçamento com serviços do catálogo e peças do estoque; reserva as peças e envia ao cliente. | `GerarOrcamentoUseCase` (lê `ServicoRepository` e `ProdutoEstoqueRepository`), `POST /api/orcamentos`, evento `OrcamentoGerado` |
| Item de serviço | Serviço do catálogo dentro do orçamento, com o nome e o valor da mão de obra copiados do catálogo no momento. | objeto de valor `ItemServico` |
| Item de peça | Peça do estoque dentro do orçamento, com quantidade e valor unitário do momento. | objeto de valor `ItemPeca` |
| Validade | Data limite para o cliente decidir. | `Orcamento.dataValidade` |
| Situação do orçamento | Pendente, aprovado, aprovado parcialmente, rejeitado ou expirado. | enum `StatusOrcamento` |
| Aprovação total | O cliente aceita todos os itens. | `APROVADO` |
| Aprovação parcial | O cliente aceita só parte dos itens; os demais saem e o total é recalculado. | `APROVADO_PARCIALMENTE`, campos `servicosAprovados` e `pecasAprovadas` |
| Reprovação / rejeição | O cliente recusa o orçamento inteiro (mesmo depois da validade, a rejeição resulta em `REJEITADO`). | `REJEITADO`, `.../rejeitar` |
| Orçamento expirado | Tentativa de aprovar depois da validade: o orçamento fica `EXPIRADO` (gravado) e a OS, Recusada. Na consulta do cliente, a expiração só aparece na resposta. | `EXPIRADO`, `Orcamento.verificarExpiracao` |
| Decisão do cliente | Aprovar ou rejeitar pela área pública, com CPF/CNPJ e código de acesso. | `POST /api/publico/orcamentos/{id}/aprovar` e `/rejeitar` |
| Decisão registrada pelo atendente | Quando o cliente decide fora do sistema (telefone, balcão). | `PATCH /api/orcamentos/{id}/aprovar` e `/rejeitar` |

### Catálogo, peças e estoque

| Termo | Significado | No código |
|---|---|---|
| Serviço | Serviço oferecido pela oficina (ex.: troca de óleo, alinhamento) com nome único, descrição e valor de mão de obra. Não pode ser excluído depois de usado em orçamento. | agregado `Servico`, `ServicoRepository`, `/api/servicos`; referenciado no orçamento por `catalogoServicoId` |
| Peça / insumo | Item físico (filtro, pastilha) ou consumível (óleo, fluido). No código, os dois são tratados igualmente; o nome é único. | agregado `ProdutoEstoque`, `/api/estoque` |
| Saldo disponível | Quantidade livre para novos orçamentos. | `ProdutoEstoque.quantidadeDisponivel` |
| Saldo reservado | Quantidade separada para orçamentos pendentes. | `ProdutoEstoque.quantidadeReservada` |
| Reservar | Separar peças ao gerar o orçamento; falha se não houver saldo. | `ProdutoEstoque.reservar` |
| Baixa de estoque | Consumir a reserva quando o orçamento é aprovado. | `ProdutoEstoque.confirmarBaixa` |
| Devolver reserva | Voltar a reserva ao disponível (rejeição, item recusado, expiração, cancelamento). | `ProdutoEstoque.cancelarReserva` |
| Entrada / saída manual | Ajuste administrativo de quantidade (compra recebida, perda, inventário). Mexe só no saldo disponível. | `ProdutoEstoque.adicionarEstoque` e `removerEstoque` (endpoints no #43) |
| Manutenção do cadastro de peças | Listar, atualizar e excluir peças e insumos. | em desenvolvimento no #43 |
| Estoque insuficiente | Saldo menor que o pedido no orçamento; impede gerar o orçamento com aquela peça. | `RegraNegocioException` em `reservar` |

### Pessoas e acesso

| Termo | Significado | No código |
|---|---|---|
| Cliente | Pessoa física ou jurídica dona do veículo. Não tem login; usa a área pública. | `Cliente`, `/api/clientes` |
| CPF/CNPJ (documento) | Identificação do cliente, validada pelos dígitos verificadores e única. | `Cliente.documento` |
| Endereço | Endereço do cliente (UF validada). | objetos de valor `Endereco` e `Estado` |
| Veículo | Automóvel do cliente. | `Veiculo`, `/api/veiculos` |
| Placa | Identificação do veículo no padrão antigo (ABC1234) ou Mercosul (ABC1D23), única. | validação em `Veiculo` |
| Atendente | Opera o sistema: cadastros, OS, orçamento, execução e entrega. | papel `ATENDENTE` |
| Administrador | Mesmas permissões do atendente e acesso ao Actuator. | papel `ADMIN` |
| Sistema | Ator automático: calcula o orçamento, reserva e baixa estoque, muda status e notifica. | casos de uso e eventos de domínio |
| Notificação | Mensagem ao cliente. No MVP, e-mail simulado no log. | `NotificadorOrdemServicoAdapter`, `NotificadorEmailAdapter` |

## Relação com o código

Itens do Miro e do egon.io que o código trata de outro jeito, e o que vale no sistema.

| No Miro / egon.io | No código | Por quê |
|---|---|---|
| Ator **Mecânico** recebe a OS, analisa o veículo, cria o relatório e executa (Miro; egon 2, 3 e 4) | O mecânico não usa o sistema. O atendente registra diagnóstico (no orçamento), início da execução e finalização. Papéis: `ADMIN` e `ATENDENTE`. | O papel `MECANICO` foi removido na migration `V6__remove_papel_mecanico.sql`; quem opera o sistema é o atendente. |
| **Relatório** do mecânico (agregado "Relatorio" no Miro) e "notificar atendente de relatório criado" | Não existe relatório separado. O resultado do diagnóstico vira os itens do orçamento. | O orçamento já documenta serviços e peças identificados. |
| "Notificar mecânico de OS criada" e ponto de atenção "como é feita essa notificação?" | Notificação só para o cliente, por e-mail simulado, a partir de eventos de domínio publicados depois do commit. | Resolve o ponto de atenção do board: o canal é e-mail, disparado pelos eventos. |
| Notificação por **WhatsApp** | E-mail simulado (log). | Escopo do MVP; não há integração com WhatsApp. |
| **Baixar o PDF** da OS | Não existe. O cliente consulta a OS e o orçamento pela API pública. | O enunciado pede consulta via API. |
| **Ordem de compra**, sistema de compras e fornecedor quando falta estoque | Não existem. Falta de saldo impede reservar a peça; a reposição é registrada como entrada manual (`/adicionar`). | Fora do escopo do MVP. |
| "Caso estoque seja insuficiente, adicionar observação à OS" | Não há observação: o orçamento não é gerado com peça sem saldo. | A reserva acontece na geração do orçamento e é atômica. |
| "Após 2 dias sem retorno, notificar cliente novamente" | Não há reenvio. O orçamento tem validade; tentar aprovar depois dela deixa o orçamento `EXPIRADO` e a OS Recusada. | Sem agendador no MVP; a expiração é verificada no momento da ação. |
| Nota "ajustar para mandar o link ao cliente quando a OS for criada e quando receber orçamento" | Implementado: o e-mail de OS criada leva o link de consulta e o código de acesso; o de orçamento gerado avisa o cliente. | Nota em aberto no board já atendida pelo código. |
| Nota "criar fluxo alternativo quando o cliente já sabe o que precisa, sem diagnóstico" | Implementado pela história 1 do egon: o atendente gera o orçamento direto; o status passa por `EM_DIAGNOSTICO` automaticamente. | Nota em aberto no board já atendida. |
| egon 5: reprovação leva a OS para **Finalizada** e depois **Entregue** | Reprovação leva a OS para `RECUSADA`, estado final. | Separa OS sem execução das executadas, inclusive no tempo médio. |
| egon 6: gerenciamento de peças com um único passo ("sistema atualiza estoque da OS aprovada") | O estoque tem reserva na geração, baixa na aprovação, devolução na rejeição/cancelamento e CRUD com entrada e saída manual. | O código é mais completo que a história; ver página 7 do `diagramas.pdf`. |
| Status "Aprovada" (egon 3 e 4) | Existe: `APROVADA`, entre `AGUARDANDO_APROVACAO` e `EM_EXECUCAO`. | Igual nos dois. |
| Status Cancelada e Recusada | Existem no código e não aparecem no Miro nem no egon. | Fluxos de exceção do código. |
| "Revisar orçamento" pelo atendente antes de enviar (egon 1 e 2) | O orçamento é enviado ao ser gerado; não há etapa de revisão. | O atendente já monta o orçamento. |
| Agregado **Cadastro** (Miro) | Dois agregados: `Cliente` e `Veiculo`, em módulos separados. | Ciclos de vida e regras próprias (documento único, placa única). |
| "Aqui ele busca por cpf/cnpj ou placa do veículo" (nota do Miro junto à lista de ordens de serviço) | A abertura da OS (`POST /api/ordens-servico`) aceita `documentoCliente` e `placaVeiculo` no lugar dos ids e confere se o veículo é do cliente. A listagem de OS filtra só por status. | O atendente identifica cliente e veículo pelo documento e pela placa; buscar OS por esses campos não foi implementado. |

## Termos evitados

| Evitar | Usar |
|---|---|
| Relatório | Orçamento (itens de serviço e peça) |
| Negar / reprovar orçamento | Rejeitar orçamento (`REJEITADO`); a OS fica Recusada |
| PDF do orçamento | Consulta pela API pública |
| Ordem de serviço atualizada (evento genérico) | O fato específico: `OrcamentoGerado`, `OrdemServicoAprovada`, `OrdemServicoFinalizada` |
