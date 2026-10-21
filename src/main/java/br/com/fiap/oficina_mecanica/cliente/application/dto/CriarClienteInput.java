package br.com.fiap.oficina_mecanica.cliente.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CriarClienteInput(
        @NotBlank(message = "é obrigatório")
        @Size(max = 150, message = "deve ter no máximo 150 caracteres") String nome,
        @NotBlank(message = "é obrigatório")
        @Size(max = 18, message = "deve ter no máximo 18 caracteres") String documento,
        @NotBlank(message = "é obrigatório")
        @Size(max = 20, message = "deve ter no máximo 20 caracteres") String telefone,
        @NotBlank(message = "é obrigatório")
        @Size(max = 150, message = "deve ter no máximo 150 caracteres") String email,
        @NotNull(message = "é obrigatório") @Valid EnderecoInput endereco
) {}
