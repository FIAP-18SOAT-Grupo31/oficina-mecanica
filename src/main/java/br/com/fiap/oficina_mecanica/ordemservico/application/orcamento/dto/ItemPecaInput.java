package br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record ItemPecaInput(
        @NotNull(message = "O ID do produto no estoque é obrigatório.")
        UUID produtoEstoqueId,
        String nomePeca,
        @NotNull(message = "A quantidade da peça é obrigatória.")
        @Positive(message = "A quantidade deve ser maior que zero.")
        Integer quantidade,
        BigDecimal valorUnitario
) {
}