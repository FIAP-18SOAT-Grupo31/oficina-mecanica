package br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.dto;

import br.com.fiap.oficina_mecanica.ordemservico.domain.orcamento.StatusOrcamento;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record OrcamentoResumoOutput(
        UUID id,
        BigDecimal valorTotal,
        StatusOrcamento status,
        LocalDateTime dataCriacao,
        LocalDateTime dataValidade
) {}
