package br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.dto;

import br.com.fiap.oficina_mecanica.ordemservico.domain.orcamento.Orcamento;

import java.util.List;

public final class OrcamentoMapper {

    private OrcamentoMapper() {
    }

    public static OrcamentoOutput toOutput(Orcamento orcamento) {
        List<ItemServicoOutput> servicos = orcamento.getServicos().stream()
                .map(s -> new ItemServicoOutput(s.catalogoServicoId(), s.descricao(), s.valorMaoDeObra()))
                .toList();

        List<ItemPecaOutput> pecas = orcamento.getPecas().stream()
                .map(p -> new ItemPecaOutput(p.produtoEstoqueId(), p.nomePeca(), p.quantidade(), p.valorUnitario()))
                .toList();

        return new OrcamentoOutput(
                orcamento.getId(),
                orcamento.getOrdemServicoId(),
                orcamento.calcularValorTotal(),
                orcamento.getStatus(),
                orcamento.getDataCriacao(),
                orcamento.getDataValidade(),
                servicos,
                pecas
        );
    }

    public static OrcamentoResumoOutput toResumo(Orcamento orcamento) {
        return new OrcamentoResumoOutput(
                orcamento.getId(),
                orcamento.calcularValorTotal(),
                orcamento.getStatus(),
                orcamento.getDataCriacao(),
                orcamento.getDataValidade()
        );
    }
}
