package br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.usecase;

import br.com.fiap.oficina_mecanica.compartilhado.domain.Horario;
import br.com.fiap.oficina_mecanica.compartilhado.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.dto.ClienteDetalheOutput;
import br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.dto.OrdemServicoDetalheOutput;
import br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.dto.VeiculoDetalheOutput;
import br.com.fiap.oficina_mecanica.ordemservico.domain.orcamento.ItemServico;
import br.com.fiap.oficina_mecanica.ordemservico.domain.orcamento.Orcamento;
import br.com.fiap.oficina_mecanica.ordemservico.domain.orcamento.StatusOrcamento;
import br.com.fiap.oficina_mecanica.ordemservico.domain.ordemservico.OrdemServico;
import br.com.fiap.oficina_mecanica.ordemservico.domain.ordemservico.OrdemServicoRepository;
import br.com.fiap.oficina_mecanica.ordemservico.domain.ordemservico.StatusOrdemServico;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BuscarOrdemServicoUseCaseTest {

    @Mock
    private OrdemServicoRepository repository;

    @Mock
    private ConsultorClientes consultorClientes;

    @Mock
    private ConsultorVeiculos consultorVeiculos;

    @InjectMocks
    private BuscarOrdemServicoUseCase useCase;

    private final UUID ordemId = UUID.randomUUID();
    private final UUID clienteId = UUID.randomUUID();
    private final UUID veiculoId = UUID.randomUUID();

    private OrdemServico ordemComOrcamentos(List<Orcamento> orcamentos) {
        return OrdemServico.builder()
                .id(ordemId).clienteId(clienteId).veiculoId(veiculoId)
                .relatoProblema("Barulho na suspensão dianteira")
                .status(StatusOrdemServico.AGUARDANDO_APROVACAO)
                .dataAbertura(Horario.agora())
                .codigoAcesso("ABC123")
                .orcamentos(new ArrayList<>(orcamentos))
                .build();
    }

    private Orcamento orcamento(StatusOrcamento status) {
        return new Orcamento(UUID.randomUUID(), ordemId, Horario.agora(), Horario.agora().plusDays(1), status,
                List.of(new ItemServico(UUID.randomUUID(), "Troca de óleo", new BigDecimal("200.00"))), List.of());
    }

    @Test
    void detalhaOrdemComClienteVeiculoECodigoDeAcesso() {
        ClienteDetalheOutput cliente = new ClienteDetalheOutput(clienteId, "Maria", "12345678900", "m@email.com", "11999990000");
        VeiculoDetalheOutput veiculo = new VeiculoDetalheOutput(veiculoId, "ABC1D23", "Fiat", "Argo", 2022);
        when(repository.buscarPorId(ordemId)).thenReturn(Optional.of(ordemComOrcamentos(List.of())));
        when(consultorClientes.detalharPorId(clienteId)).thenReturn(Optional.of(cliente));
        when(consultorVeiculos.detalharPorId(veiculoId)).thenReturn(Optional.of(veiculo));

        OrdemServicoDetalheOutput output = useCase.execute(ordemId);

        assertThat(output.id()).isEqualTo(ordemId);
        assertThat(output.status()).isEqualTo(StatusOrdemServico.AGUARDANDO_APROVACAO);
        assertThat(output.relatoProblema()).isEqualTo("Barulho na suspensão dianteira");
        assertThat(output.codigoAcesso()).isEqualTo("ABC123");
        assertThat(output.cliente()).isEqualTo(cliente);
        assertThat(output.veiculo()).isEqualTo(veiculo);
        assertThat(output.orcamentos()).isEmpty();
    }

    @Test
    void incluiTodosOsOrcamentosVinculadosIndependenteDoStatus() {
        Orcamento rejeitado = orcamento(StatusOrcamento.REJEITADO);
        Orcamento pendente = orcamento(StatusOrcamento.PENDENTE);
        when(repository.buscarPorId(ordemId)).thenReturn(Optional.of(ordemComOrcamentos(List.of(rejeitado, pendente))));
        when(consultorClientes.detalharPorId(clienteId)).thenReturn(Optional.empty());
        when(consultorVeiculos.detalharPorId(veiculoId)).thenReturn(Optional.empty());

        OrdemServicoDetalheOutput output = useCase.execute(ordemId);

        assertThat(output.orcamentos()).hasSize(2);
        assertThat(output.orcamentos().get(0).id()).isEqualTo(rejeitado.getId());
        assertThat(output.orcamentos().get(0).status()).isEqualTo(StatusOrcamento.REJEITADO);
        assertThat(output.orcamentos().get(0).valorTotal()).isEqualByComparingTo("200.00");
        assertThat(output.orcamentos().get(0).servicos()).hasSize(1);
        assertThat(output.orcamentos().get(1).status()).isEqualTo(StatusOrcamento.PENDENTE);
    }

    @Test
    void clienteOuVeiculoNaoEncontradosFicamNulos() {
        when(repository.buscarPorId(ordemId)).thenReturn(Optional.of(ordemComOrcamentos(List.of())));
        when(consultorClientes.detalharPorId(clienteId)).thenReturn(Optional.empty());
        when(consultorVeiculos.detalharPorId(veiculoId)).thenReturn(Optional.empty());

        OrdemServicoDetalheOutput output = useCase.execute(ordemId);

        assertThat(output.cliente()).isNull();
        assertThat(output.veiculo()).isNull();
    }

    @Test
    void ordemInexistenteViraRecursoNaoEncontrado() {
        when(repository.buscarPorId(ordemId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(ordemId))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Ordem de Serviço não encontrada.");
    }
}
