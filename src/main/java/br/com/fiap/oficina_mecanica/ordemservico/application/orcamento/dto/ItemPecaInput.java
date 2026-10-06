package br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ItemPecaInput(UUID produtoEstoqueId, String nomePeca, Integer quantidade, BigDecimal valorUnitario) {}