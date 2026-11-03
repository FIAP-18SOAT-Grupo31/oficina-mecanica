package br.com.fiap.oficina_mecanica.cliente.infrastructure;

import br.com.fiap.oficina_mecanica.veiculo.domain.VeiculoRepository;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VerificadorVeiculosDoClienteAdapterTest {

    private final VeiculoRepository veiculoRepository = mock(VeiculoRepository.class);
    private final VerificadorVeiculosDoClienteAdapter adapter =
            new VerificadorVeiculosDoClienteAdapter(veiculoRepository);

    @Test
    void possuiVeiculosQuandoORepositorioEncontraVeiculosDoCliente() {
        UUID clienteId = UUID.randomUUID();
        when(veiculoRepository.existePorClienteId(clienteId)).thenReturn(true);

        assertThat(adapter.possuiVeiculos(clienteId)).isTrue();
    }

    @Test
    void naoPossuiVeiculosQuandoORepositorioNaoEncontra() {
        UUID clienteId = UUID.randomUUID();
        when(veiculoRepository.existePorClienteId(clienteId)).thenReturn(false);

        assertThat(adapter.possuiVeiculos(clienteId)).isFalse();
    }
}
