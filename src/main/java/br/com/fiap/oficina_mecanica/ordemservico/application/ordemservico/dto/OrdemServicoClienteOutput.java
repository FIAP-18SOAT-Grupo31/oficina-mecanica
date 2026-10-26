package br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.dto;

import br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.dto.OrcamentoOutput;
import br.com.fiap.oficina_mecanica.ordemservico.domain.ordemservico.OrdemServico;
import br.com.fiap.oficina_mecanica.ordemservico.domain.ordemservico.StatusOrdemServico;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrdemServicoClienteOutput(
        UUID id,
        StatusOrdemServico status,
        String relatoProblema,
        LocalDateTime dataAbertura,
        LocalDateTime dataConclusao,
        VeiculoDetalheOutput veiculo,
        List<OrcamentoOutput> orcamentos
) {

    public static OrdemServicoClienteOutput de(OrdemServico ordemServico, VeiculoDetalheOutput veiculo,
                                               List<OrcamentoOutput> orcamentos) {
        return new OrdemServicoClienteOutput(
                ordemServico.getId(),
                ordemServico.getStatus(),
                ordemServico.getRelatoProblema(),
                ordemServico.getDataAbertura(),
                ordemServico.getDataConclusao(),
                veiculo,
                orcamentos);
    }
}
