package br.com.fiap.oficina_mecanica.ordemservico.domain;

import java.math.BigDecimal;
import java.util.UUID;

public record ItemPeca(UUID produtoEstoqueId, String nomePeca, Integer quantidade, BigDecimal valorUnitario) {
    public BigDecimal calcularTotal() {
        return valorUnitario.multiply(BigDecimal.valueOf(quantidade));
    }
}