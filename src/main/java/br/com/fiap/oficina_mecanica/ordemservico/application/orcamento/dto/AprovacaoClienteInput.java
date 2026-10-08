package br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.dto;

import java.util.List;
import java.util.UUID;

public record AprovacaoClienteInput(
        String cpf,
        String codigoAcesso,
        List<UUID> servicosAprovados,
        List<UUID> pecasAprovadas
) {}