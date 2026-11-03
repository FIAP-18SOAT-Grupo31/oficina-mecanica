package br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.dto;

import java.util.List;
import java.util.UUID;

public record AprovacaoInput(
        List<UUID> servicosAprovados,
        List<UUID> pecasAprovadas
) {}