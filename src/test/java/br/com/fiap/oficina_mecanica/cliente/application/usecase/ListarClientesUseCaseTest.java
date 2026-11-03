package br.com.fiap.oficina_mecanica.cliente.application.usecase;

import br.com.fiap.oficina_mecanica.cliente.application.dto.ClienteOutput;
import br.com.fiap.oficina_mecanica.cliente.application.dto.ListarClientesInput;
import br.com.fiap.oficina_mecanica.cliente.domain.Cliente;
import br.com.fiap.oficina_mecanica.cliente.domain.ClienteRepository;
import br.com.fiap.oficina_mecanica.compartilhado.application.dto.PaginaOutput;
import br.com.fiap.oficina_mecanica.compartilhado.domain.Pagina;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ListarClientesUseCaseTest {

    private final ClienteRepository repository = mock(ClienteRepository.class);
    private final ListarClientesUseCase useCase = new ListarClientesUseCase(repository);

    @Test
    void normalizaOsFiltrosEConverteAPaginaParaOutput() {
        Cliente cliente = ClienteFixtures.cliente();
        when(repository.listar("maria", "52998224725", 1, 5))
                .thenReturn(new Pagina<>(List.of(cliente), 1, 5, 6));

        PaginaOutput<ClienteOutput> output =
                useCase.execute(new ListarClientesInput("  maria  ", "529.982.247-25", 1, 5));

        verify(repository).listar("maria", "52998224725", 1, 5);
        assertThat(output.conteudo()).hasSize(1);
        assertThat(output.conteudo().get(0).id()).isEqualTo(cliente.getId());
        assertThat(output.pagina()).isEqualTo(1);
        assertThat(output.tamanho()).isEqualTo(5);
        assertThat(output.totalElementos()).isEqualTo(6);
        assertThat(output.totalPaginas()).isEqualTo(2);
    }

    @Test
    void documentoComLetrasFicaEmMaiusculas() {
        when(repository.listar(null, "12ABC34501DE35", 0, 20)).thenReturn(new Pagina<>(List.of(), 0, 20, 0));

        useCase.execute(new ListarClientesInput(null, "12.abc.345/01de-35", 0, 20));

        verify(repository).listar(null, "12ABC34501DE35", 0, 20);
    }

    @Test
    void filtrosNulosOuEmBrancoViramSemFiltro() {
        when(repository.listar(null, null, 0, 20)).thenReturn(new Pagina<>(List.of(), 0, 20, 0));

        PaginaOutput<ClienteOutput> semFiltros = useCase.execute(new ListarClientesInput(null, null, 0, 20));
        PaginaOutput<ClienteOutput> emBranco = useCase.execute(new ListarClientesInput("   ", "  ", 0, 20));

        assertThat(semFiltros.conteudo()).isEmpty();
        assertThat(semFiltros.totalPaginas()).isZero();
        assertThat(emBranco.conteudo()).isEmpty();
    }
}
