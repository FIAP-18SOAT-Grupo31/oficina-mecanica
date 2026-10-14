package br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.dto;

import br.com.fiap.oficina_mecanica.ordemservico.domain.orcamento.Orcamento;

public class OrcamentoMapper {
    public static OrcamentoOutput toOutput(Orcamento orcamento) {
        return new OrcamentoOutput(
                orcamento.getId(),
                orcamento.getOrdemServicoId(),
                orcamento.calcularValorTotal(),
                orcamento.getStatus(),
                orcamento.getDataCriacao(),
                orcamento.getDataValidade()
        );
    }
}