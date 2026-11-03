package br.com.fiap.oficina_mecanica.servico.application.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CriarServicoInput(
        @NotBlank(message = "O nome do serviço é obrigatório.")
        @Size(max = 100, message = "O nome do serviço deve ter no máximo 100 caracteres.")
        String nome,
        @Size(max = 255, message = "A descrição do serviço deve ter no máximo 255 caracteres.")
        String descricao,
        @NotNull(message = "O valor da mão de obra é obrigatório.")
        @Positive(message = "O valor da mão de obra deve ser maior que zero.")
        @Digits(integer = 10, fraction = 2, message = "O valor da mão de obra deve ter no máximo 2 casas decimais.")
        BigDecimal valorMaoDeObra
) {}
