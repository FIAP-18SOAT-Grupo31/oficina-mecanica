package br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.dto;

import br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.dto.OrcamentoOutput;
import br.com.fiap.oficina_mecanica.ordemservico.domain.ordemservico.OrdemServico;
import br.com.fiap.oficina_mecanica.ordemservico.domain.ordemservico.StatusOrdemServico;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrdemServicoDetalheOutput(
        UUID id,
        StatusOrdemServico status,
        String relatoProblema,
        LocalDateTime dataAbertura,
        LocalDateTime dataConclusao,
        String codigoAcesso,
        ClienteDetalheOutput cliente,
        VeiculoDetalheOutput veiculo,
        List<OrcamentoOutput> orcamentos
) {

    public static OrdemServicoDetalheOutput de(OrdemServico ordemServico, ClienteDetalheOutput cliente,
                                               VeiculoDetalheOutput veiculo, List<OrcamentoOutput> orcamentos) {
        return new OrdemServicoDetalheOutput(
                ordemServico.getId(),
                ordemServico.getStatus(),
                ordemServico.getRelatoProblema(),
                ordemServico.getDataAbertura(),
                ordemServico.getDataConclusao(),
                ordemServico.getCodigoAcesso(),
                cliente,
                veiculo,
                orcamentos);
    }
}
