package br.com.fiap.oficina_mecanica.ordemservico.domain.orcamento;

import java.math.BigDecimal;
import java.util.UUID;

public record ItemServico(UUID catalogoServicoId, String descricao, BigDecimal valorMaoDeObra) {
    public BigDecimal calcularTotal() {
        return valorMaoDeObra;
    }
}