package br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.UUID;

public record AprovacaoClienteInput(
        @NotBlank(message = "O CPF é obrigatório.")
        String cpf,
        @NotBlank(message = "O código de acesso é obrigatório.")
        String codigoAcesso,
        List<UUID> servicosAprovados,
        List<UUID> pecasAprovadas
) {
}