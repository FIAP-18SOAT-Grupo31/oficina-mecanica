package br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Serviço do catálogo incluído no orçamento. O nome e o valor da mão de obra vêm do catálogo, não do cliente da API.
 */
public record ItemServicoInput(
        @NotNull(message = "O ID do serviço no catálogo é obrigatório.")
        UUID catalogoServicoId
) {
}
