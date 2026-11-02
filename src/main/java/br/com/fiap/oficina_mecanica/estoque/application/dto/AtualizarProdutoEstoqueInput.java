package br.com.fiap.oficina_mecanica.estoque.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record AtualizarProdutoEstoqueInput(
        @NotBlank(message = "O nome da peça é obrigatório.")
        String nomePeca,

        @NotNull(message = "O valor unitario da peça é obrigatório.")
        @Positive(message = "O valor unitario deve ser um valor positivo.")
        BigDecimal valorUnitario
) {
    public AtualizarProdutoEstoqueInput {
        if (nomePeca != null && !nomePeca.trim().isEmpty()) {
            nomePeca = nomePeca.trim().toUpperCase();
        }
    }
}
