package br.com.fiap.oficina_mecanica.servico.infrastructure;

import br.com.fiap.oficina_mecanica.servico.application.usecase.VerificadorUsoDoServico;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class VerificadorUsoDoServicoAdapter implements VerificadorUsoDoServico {

    private static final String CONSULTA =
            "SELECT COUNT(*) FROM orcamento_itens_servico WHERE catalogo_servico_id = ?";

    private final JdbcTemplate jdbc;

    public VerificadorUsoDoServicoAdapter(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public boolean usadoEmOrcamento(UUID servicoId) {
        Long quantidade = jdbc.queryForObject(CONSULTA, Long.class, servicoId);
        return quantidade != null && quantidade > 0;
    }
}
