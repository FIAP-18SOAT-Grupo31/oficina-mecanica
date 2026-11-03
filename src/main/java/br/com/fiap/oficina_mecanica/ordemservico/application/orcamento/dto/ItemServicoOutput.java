package br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ItemServicoOutput(
        UUID catalogoServicoId,
        String descricao,
        BigDecimal valorMaoDeObra
) {}