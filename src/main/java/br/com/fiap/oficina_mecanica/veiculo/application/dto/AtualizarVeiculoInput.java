package br.com.fiap.oficina_mecanica.veiculo.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record AtualizarVeiculoInput(
        @NotNull(message = "é obrigatório") UUID clienteId,
        @NotBlank(message = "é obrigatório")
        @Size(max = 8, message = "deve ter no máximo 8 caracteres") String placa,
        @NotBlank(message = "é obrigatório")
        @Size(max = 50, message = "deve ter no máximo 50 caracteres") String marca,
        @NotBlank(message = "é obrigatório")
        @Size(max = 50, message = "deve ter no máximo 50 caracteres") String modelo,
        @NotNull(message = "é obrigatório") Integer anoFabricacao
) {}
