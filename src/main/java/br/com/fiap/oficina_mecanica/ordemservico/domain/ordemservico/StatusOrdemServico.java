package br.com.fiap.oficina_mecanica.ordemservico.domain.ordemservico;

public enum StatusOrdemServico {
    RECEBIDA,
    EM_DIAGNOSTICO,
    AGUARDANDO_APROVACAO,
    APROVADA,
    RECUSADA,
    EM_EXECUCAO,
    FINALIZADA,
    ENTREGUE,
    CANCELADA
}