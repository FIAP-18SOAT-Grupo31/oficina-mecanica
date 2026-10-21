package br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record ItemServicoInput(
        @NotNull(message = "O ID do serviço no catálogo é obrigatório.")
        UUID catalogoServicoId,
        @NotBlank(message = "A descrição do serviço é obrigatória.")
        String descricao,
        @NotNull(message = "O valor da mão de obra é obrigatório.")
        @Positive(message = "O valor da mão de obra deve ser maior que zero.")
        BigDecimal valorMaoDeObra
) {
}