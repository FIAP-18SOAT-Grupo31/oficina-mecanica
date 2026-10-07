package br.com.fiap.oficina_mecanica.autenticacao.application.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginInput(
        @NotBlank(message = "é obrigatório") String login,
        @NotBlank(message = "é obrigatório") String senha
) {}
