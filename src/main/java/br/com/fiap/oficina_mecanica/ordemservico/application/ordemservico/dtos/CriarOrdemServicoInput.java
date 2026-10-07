package br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.dtos;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CriarOrdemServicoInput(
    @NotNull UUID clienteId,
    @NotNull UUID veiculoId,
    @NotBlank String relatoProblema
) {

}
