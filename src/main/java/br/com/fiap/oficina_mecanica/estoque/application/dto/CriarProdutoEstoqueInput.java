package br.com.fiap.oficina_mecanica.estoque.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record CriarProdutoEstoqueInput(
        @NotBlank(message = "O nome da peça é obrigatório.")
        String nomePeca,

        @NotNull(message = "O valor unitario da peça é obrigatório.")
        @Positive(message = "O valor unitario deve ser um valor positivo.")
        BigDecimal valorUnitario,

        @NotNull(message = "A quantidade disponivel é obrigatória.")
        @PositiveOrZero(message = "A quantidade deve ser zero ou maior.")
        Integer quantidadeDisponivel


) {
    public CriarProdutoEstoqueInput {
        if (nomePeca != null && !nomePeca.trim().isEmpty()) {
            nomePeca = nomePeca.trim().toUpperCase();
        }
    }

}
