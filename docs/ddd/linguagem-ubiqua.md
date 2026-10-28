# Linguagem ubíqua

Vocabulário usado no Event Storming (Miro), nas histórias do egon.io e no código.

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
| Tempo médio de execução | Média do tempo entre o início da execução e a finalização das OS. | ainda não implementado; as datas de cada mudança já ficam em `historico_status_ordem_servico` |
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
| Entrada / saída manual | Ajuste administrativo de quantidade (compra recebida, perda, inventário). Mexe só no saldo disponível. | `ProdutoEstoque.adicionarEstoque` e `removerEstoque`, `POST /api/estoque/{id}/adicionar` e `/remover` |
| Cadastro de peças | Criar a peça e consultar por id ou por nome. Listagem, atualização e exclusão ainda não existem. | `POST /api/estoque`, `GET /api/estoque/{id}`, `GET /api/estoque/peca?nomePeca=` |
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

## Mapa de contextos

Desenhado na página 9 do [`diagramas.pdf`](diagramas.pdf). U é o contexto upstream (fornecedor) e D o downstream (cliente, que consome o U).

| Relação | Padrão |
|---|---|
| Clientes (U) → Atendimento e execução (D) | camada anticorrupção: portas `VerificadorCliente` e `ConsultorClientes` no núcleo |
| Veículos (U) → Atendimento e execução (D) | camada anticorrupção: portas `VerificadorVeiculo` e `ConsultorVeiculos` |
| Estoque (U) → Atendimento e execução (D) | cliente-fornecedor: o orçamento usa `ProdutoEstoqueRepository` na mesma transação |
| Catálogo (U) → Atendimento e execução (D) | conformista: o orçamento lê `ServicoRepository` e copia nome e valor |
| Autenticação (U) → demais contextos (D) | genérico: JWT nas rotas administrativas |
| Atendimento e execução (U) → E-mail ao cliente (D) | eventos de domínio depois do commit, e-mail simulado no log |

O módulo `compartilhado` é só utilitário técnico (paginação, exceções, publicação de eventos e padrão de erros). Não é um shared kernel: nenhum modelo de domínio é compartilhado entre os contextos.

## Relação com o código

Principais pontos em que o código ficou diferente do Miro e do egon.io.

| No Miro / egon.io | No código | Por quê |
|---|---|---|
| Mecânico recebe a OS, analisa o veículo e executa (Miro; egon 2, 3 e 4) | O mecânico não usa o sistema. O atendente registra orçamento, início da execução e finalização. | O papel `MECANICO` saiu na `V6__remove_papel_mecanico.sql`. |
| Relatório do mecânico (agregado "Relatorio") | Não existe. O diagnóstico vira os itens do orçamento. | O orçamento já lista serviços e peças. |
| "Notificar mecânico de OS criada" e o ponto de atenção "como é feita essa notificação?" | Só o cliente é avisado, por e-mail simulado, depois do commit. | O aviso ao mecânico ficou fora do sistema; implementamos avisos ao cliente. |
| WhatsApp e PDF da OS | E-mail simulado no log; o cliente consulta pela API pública. | Fora do escopo do MVP. |
| Ordem de compra e fornecedor quando falta estoque | Sem saldo, o orçamento não é gerado; a reposição é entrada manual (`/adicionar`). | Fora do escopo do MVP. |
| egon 5: reprovação leva a OS para Finalizada e Entregue | Reprovação deixa a OS `RECUSADA`, estado final. | Separa OS sem execução das executadas. |

## Termos evitados

| Evitar | Usar |
|---|---|
| Relatório | Orçamento (itens de serviço e peça) |
| Negar / reprovar orçamento | Rejeitar orçamento (`REJEITADO`); a OS fica Recusada |
| PDF do orçamento | Consulta pela API pública |
| Ordem de serviço atualizada (evento genérico) | O fato específico: `OrcamentoGerado`, `OrdemServicoAprovada`, `OrdemServicoFinalizada` |
