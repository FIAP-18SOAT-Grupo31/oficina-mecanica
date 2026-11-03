package br.com.fiap.oficina_mecanica.veiculo.application.usecase;

import br.com.fiap.oficina_mecanica.compartilhado.application.dto.PaginaOutput;
import br.com.fiap.oficina_mecanica.compartilhado.domain.Pagina;
import br.com.fiap.oficina_mecanica.veiculo.application.dto.ListarVeiculosInput;
import br.com.fiap.oficina_mecanica.veiculo.application.dto.VeiculoOutput;
import br.com.fiap.oficina_mecanica.veiculo.domain.Veiculo;
import br.com.fiap.oficina_mecanica.veiculo.domain.VeiculoRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ListarVeiculosUseCaseTest {

    private final VeiculoRepository repository = mock(VeiculoRepository.class);
    private final ListarVeiculosUseCase useCase = new ListarVeiculosUseCase(repository);

    @Test
    void normalizaOsFiltrosEConverteAPaginaParaOutput() {
        Veiculo veiculo = VeiculoFixtures.veiculo();
        when(repository.listar(VeiculoFixtures.CLIENTE_ID, "ABC1D23", "argo", 1, 5))
                .thenReturn(new Pagina<>(List.of(veiculo), 1, 5, 6));

        PaginaOutput<VeiculoOutput> output = useCase.execute(
                new ListarVeiculosInput(VeiculoFixtures.CLIENTE_ID, "abc-1d23", "  argo  ", 1, 5));

        verify(repository).listar(VeiculoFixtures.CLIENTE_ID, "ABC1D23", "argo", 1, 5);
        assertThat(output.conteudo()).hasSize(1);
        assertThat(output.conteudo().get(0).id()).isEqualTo(veiculo.getId());
        assertThat(output.totalElementos()).isEqualTo(6);
        assertThat(output.totalPaginas()).isEqualTo(2);
    }

    @Test
    void filtrosNulosOuEmBrancoViramSemFiltro() {
        when(repository.listar(null, null, null, 0, 20)).thenReturn(new Pagina<>(List.of(), 0, 20, 0));

        PaginaOutput<VeiculoOutput> semFiltros = useCase.execute(new ListarVeiculosInput(null, null, null, 0, 20));
        PaginaOutput<VeiculoOutput> emBranco = useCase.execute(new ListarVeiculosInput(null, "  ", "   ", 0, 20));

        assertThat(semFiltros.conteudo()).isEmpty();
        assertThat(emBranco.conteudo()).isEmpty();
    }
}
