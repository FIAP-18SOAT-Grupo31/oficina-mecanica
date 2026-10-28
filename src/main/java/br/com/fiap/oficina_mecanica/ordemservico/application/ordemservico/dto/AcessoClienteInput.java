package br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.dto;

import jakarta.validation.constraints.NotBlank;

public record AcessoClienteInput(
        @NotBlank(message = "O CPF é obrigatório.")
        String cpf,
        @NotBlank(message = "O código de acesso é obrigatório.")
        String codigoAcesso
) {}
