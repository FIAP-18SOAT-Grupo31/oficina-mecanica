package br.com.fiap.oficina_mecanica.ordemservico.infrastructure.ordemservico;

import br.com.fiap.oficina_mecanica.cliente.domain.ClienteRepository;
import br.com.fiap.oficina_mecanica.cliente.domain.ClienteResumo;
import br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.dto.ClienteResumoOutput;
import br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.dto.VeiculoResumoOutput;
import br.com.fiap.oficina_mecanica.veiculo.domain.VeiculoRepository;
import br.com.fiap.oficina_mecanica.veiculo.domain.VeiculoResumo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultoresAdapterTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private VeiculoRepository veiculoRepository;

    @Test
    void consultorDeClientesIndexaResumosPorId() {
        UUID id = UUID.randomUUID();
        when(clienteRepository.buscarResumosPorIds(List.of(id))).thenReturn(List.of(new ClienteResumo(id, "Maria")));

        Map<UUID, ClienteResumoOutput> resumos = new ConsultorClientesAdapter(clienteRepository).resumirPorIds(List.of(id));

        assertThat(resumos).containsOnlyKeys(id);
        assertThat(resumos.get(id)).isEqualTo(new ClienteResumoOutput(id, "Maria"));
    }

    @Test
    void consultorDeClientesSemResultadoDevolveMapaVazio() {
        UUID id = UUID.randomUUID();
        when(clienteRepository.buscarResumosPorIds(List.of(id))).thenReturn(List.of());

        assertThat(new ConsultorClientesAdapter(clienteRepository).resumirPorIds(List.of(id))).isEmpty();
    }

    @Test
    void consultorDeVeiculosIndexaResumosPorId() {
        UUID id = UUID.randomUUID();
        when(veiculoRepository.buscarResumosPorIds(List.of(id)))
                .thenReturn(List.of(new VeiculoResumo(id, "ABC1D23", "Fiat", "Argo")));

        Map<UUID, VeiculoResumoOutput> resumos = new ConsultorVeiculosAdapter(veiculoRepository).resumirPorIds(List.of(id));

        assertThat(resumos).containsOnlyKeys(id);
        assertThat(resumos.get(id)).isEqualTo(new VeiculoResumoOutput(id, "ABC1D23", "Fiat", "Argo"));
    }

    @Test
    void consultorDeVeiculosSemResultadoDevolveMapaVazio() {
        UUID id = UUID.randomUUID();
        when(veiculoRepository.buscarResumosPorIds(List.of(id))).thenReturn(List.of());

        assertThat(new ConsultorVeiculosAdapter(veiculoRepository).resumirPorIds(List.of(id))).isEmpty();
    }
}
