package br.com.fiap.oficina_mecanica.ordemservico.domain;

public enum StatusOS {
    RECEBIDA,
    EM_DIAGNOSTICO,
    AGUARDANDO_APROVACAO,
    APROVADA,
    RECUSADA,
    EM_EXECUCAO,
    FINALIZADA,
    ENTREGUE
}