package br.com.fiap.oficina_mecanica.veiculo.application.dto;

import br.com.fiap.oficina_mecanica.veiculo.domain.Veiculo;

import java.util.UUID;

public record VeiculoOutput(
        UUID id,
        UUID clienteId,
        String placa,
        String marca,
        String modelo,
        int anoFabricacao
) {

    public static VeiculoOutput de(Veiculo veiculo) {
        return new VeiculoOutput(
                veiculo.getId(),
                veiculo.getClienteId(),
                veiculo.getPlaca(),
                veiculo.getMarca(),
                veiculo.getModelo(),
                veiculo.getAnoFabricacao()
        );
    }
}
