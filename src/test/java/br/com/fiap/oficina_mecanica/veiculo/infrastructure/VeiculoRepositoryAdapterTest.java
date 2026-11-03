package br.com.fiap.oficina_mecanica.veiculo.infrastructure;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VeiculoRepositoryAdapterTest {

    private final VeiculoJpaRepository jpaRepository = mock(VeiculoJpaRepository.class);
    private final VeiculoRepositoryAdapter adapter = new VeiculoRepositoryAdapter(jpaRepository);

    @Test
    void existePorIdDelegaAoRepositorioJpa() {
        UUID id = UUID.randomUUID();
        when(jpaRepository.existsById(id)).thenReturn(true);

        assertThat(adapter.existePorId(id)).isTrue();
        assertThat(adapter.existePorId(UUID.randomUUID())).isFalse();
    }

    @Test
    void existePorClienteIdDelegaAoRepositorioJpa() {
        UUID clienteId = UUID.randomUUID();
        when(jpaRepository.existsByClienteId(clienteId)).thenReturn(true);

        assertThat(adapter.existePorClienteId(clienteId)).isTrue();
        assertThat(adapter.existePorClienteId(UUID.randomUUID())).isFalse();
    }
}
