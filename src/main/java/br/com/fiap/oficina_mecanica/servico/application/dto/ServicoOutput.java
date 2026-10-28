package br.com.fiap.oficina_mecanica.servico.application.dto;

import br.com.fiap.oficina_mecanica.servico.domain.Servico;

import java.math.BigDecimal;
import java.util.UUID;

public record ServicoOutput(UUID id, String nome, String descricao, BigDecimal valorMaoDeObra) {

    public static ServicoOutput de(Servico servico) {
        return new ServicoOutput(
                servico.getId(),
                servico.getNome(),
                servico.getDescricao(),
                servico.getValorMaoDeObra()
        );
    }
}
