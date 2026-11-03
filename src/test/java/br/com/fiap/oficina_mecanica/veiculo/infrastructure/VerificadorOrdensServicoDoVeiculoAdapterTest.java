package br.com.fiap.oficina_mecanica.veiculo.infrastructure;

import br.com.fiap.oficina_mecanica.ordemservico.domain.ordemservico.OrdemServicoRepository;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VerificadorOrdensServicoDoVeiculoAdapterTest {

    private final OrdemServicoRepository ordemServicoRepository = mock(OrdemServicoRepository.class);
    private final VerificadorOrdensServicoDoVeiculoAdapter adapter =
            new VerificadorOrdensServicoDoVeiculoAdapter(ordemServicoRepository);

    @Test
    void delegaAConsultaDasOrdensDeServicoAoRepositorio() {
        UUID comOrdem = UUID.randomUUID();
        when(ordemServicoRepository.existePorVeiculoId(comOrdem)).thenReturn(true);

        assertThat(adapter.possuiOrdensServico(comOrdem)).isTrue();
        assertThat(adapter.possuiOrdensServico(UUID.randomUUID())).isFalse();
    }
}
