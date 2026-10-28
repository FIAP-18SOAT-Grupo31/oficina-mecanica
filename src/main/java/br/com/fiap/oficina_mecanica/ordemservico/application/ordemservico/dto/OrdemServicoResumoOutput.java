package br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.dto;

import br.com.fiap.oficina_mecanica.ordemservico.domain.ordemservico.OrdemServico;
import br.com.fiap.oficina_mecanica.ordemservico.domain.ordemservico.StatusOrdemServico;

import java.time.LocalDateTime;
import java.util.UUID;

public record OrdemServicoResumoOutput(
        UUID id,
        StatusOrdemServico status,
        String relatoProblema,
        LocalDateTime dataAbertura,
        LocalDateTime dataConclusao,
        ClienteResumoOutput cliente,
        VeiculoResumoOutput veiculo
) {

    public static OrdemServicoResumoOutput de(OrdemServico ordemServico, ClienteResumoOutput cliente,
                                              VeiculoResumoOutput veiculo) {
        return new OrdemServicoResumoOutput(
                ordemServico.getId(),
                ordemServico.getStatus(),
                ordemServico.getRelatoProblema(),
                ordemServico.getDataAbertura(),
                ordemServico.getDataConclusao(),
                cliente,
                veiculo);
    }
}
