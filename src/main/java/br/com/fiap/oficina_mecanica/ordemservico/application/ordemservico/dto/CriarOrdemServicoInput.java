package br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CriarOrdemServicoInput(
        @NotNull(message = "é obrigatório") UUID clienteId,
        @NotNull(message = "é obrigatório") UUID veiculoId,
        @NotBlank(message = "é obrigatório")
        @Size(min = 10, max = 500, message = "deve ter entre 10 e 500 caracteres") String relatoProblema
) {}
