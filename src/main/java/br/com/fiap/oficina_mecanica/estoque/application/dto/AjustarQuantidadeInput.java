package br.com.fiap.oficina_mecanica.estoque.application.dto;

import jakarta.validation.constraints.PositiveOrZero;

public record AjustarQuantidadeInput(
        @PositiveOrZero(message = "A quantidade deve ser zero ou um valor positivo.")
        Integer quantidade
) {
}