package br.com.fiap.oficina_mecanica.veiculo.infrastructure;

import br.com.fiap.oficina_mecanica.cliente.domain.ClienteRepository;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VerificadorDonoDoVeiculoAdapterTest {

    private final ClienteRepository clienteRepository = mock(ClienteRepository.class);
    private final VerificadorDonoDoVeiculoAdapter adapter = new VerificadorDonoDoVeiculoAdapter(clienteRepository);

    @Test
    void delegaAExistenciaDoClienteAoRepositorio() {
        UUID existente = UUID.randomUUID();
        when(clienteRepository.existePorId(existente)).thenReturn(true);

        assertThat(adapter.existe(existente)).isTrue();
        assertThat(adapter.existe(UUID.randomUUID())).isFalse();
    }
}
