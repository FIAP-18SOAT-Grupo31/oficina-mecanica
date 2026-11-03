package br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ItemServicoInput(
        @NotNull(message = "O ID do serviço no catálogo é obrigatório.")
        UUID catalogoServicoId
) {
}
