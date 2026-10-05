package br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record GerarOrcamentoInput(
        UUID ordemServicoId,
        LocalDateTime dataValidade,
        List<ItemServicoInput> servicos,
        List<ItemPecaInput> pecas
) {}