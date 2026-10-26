package br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.usecase;

import br.com.fiap.oficina_mecanica.compartilhado.domain.Horario;
import br.com.fiap.oficina_mecanica.compartilhado.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.oficina_mecanica.compartilhado.domain.exception.RegraNegocioException;
import br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.usecase.VerificadorAcessoCliente;
import br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.dto.OrcamentoResumoOutput;
import br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.dto.OrdemServicoClienteOutput;
import br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.dto.VeiculoDetalheOutput;
import br.com.fiap.oficina_mecanica.ordemservico.domain.orcamento.Orcamento;
import br.com.fiap.oficina_mecanica.ordemservico.domain.orcamento.StatusOrcamento;
import br.com.fiap.oficina_mecanica.ordemservico.domain.ordemservico.OrdemServico;
import br.com.fiap.oficina_mecanica.ordemservico.domain.ordemservico.OrdemServicoRepository;
import br.com.fiap.oficina_mecanica.ordemservico.domain.ordemservico.StatusOrdemServico;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultarOrdemServicoClienteUseCaseTest {

    @Mock
    private OrdemServicoRepository repository;

    @Mock
    private VerificadorAcessoCliente verificadorAcesso;

    @Mock
    private ConsultorVeiculos consultorVeiculos;

    private final UUID ordemId = UUID.randomUUID();
    private final UUID veiculoId = UUID.randomUUID();

    private ConsultarOrdemServicoClienteUseCase useCase() {
        return new ConsultarOrdemServicoClienteUseCase(repository, verificadorAcesso, consultorVeiculos);
    }

    private OrdemServico ordemComOrcamentos(List<Orcamento> orcamentos) {
        return OrdemServico.builder()
                .id(ordemId).clienteId(UUID.randomUUID()).veiculoId(veiculoId)
                .relatoProblema("Barulho na suspensão dianteira")
                .status(StatusOrdemServico.AGUARDANDO_APROVACAO)
                .dataAbertura(Horario.agora())
                .codigoAcesso("ABC123")
                .orcamentos(new ArrayList<>(orcamentos))
                .build();
    }

    private Orcamento orcamento(StatusOrcamento status, LocalDateTime validade) {
        return orcamento(status, Horario.agora().minusDays(2), validade);
    }

    private Orcamento orcamento(StatusOrcamento status, LocalDateTime criacao, LocalDateTime validade) {
        return new Orcamento(UUID.randomUUID(), ordemId, criacao, validade, status, List.of(), List.of());
    }

    @Test
    void devolveOrdemComVeiculoEResumoDoUltimoOrcamento() {
        Orcamento antigo = orcamento(StatusOrcamento.REJEITADO, Horario.agora().minusDays(5), Horario.agora().plusDays(1));
        Orcamento recente = orcamento(StatusOrcamento.PENDENTE, Horario.agora().minusDays(1), Horario.agora().plusDays(1));
        VeiculoDetalheOutput veiculo = new VeiculoDetalheOutput(veiculoId, "ABC1D23", "Fiat", "Argo", 2022);
        when(repository.buscarPorId(ordemId)).thenReturn(Optional.of(ordemComOrcamentos(List.of(recente, antigo))));
        when(consultorVeiculos.detalharPorId(veiculoId)).thenReturn(Optional.of(veiculo));

        OrdemServicoClienteOutput output = useCase().execute(ordemId, "123.456.789-00", "ABC123");

        verify(verificadorAcesso).validar(ordemId, "123.456.789-00", "ABC123");
        assertThat(output.id()).isEqualTo(ordemId);
        assertThat(output.status()).isEqualTo(StatusOrdemServico.AGUARDANDO_APROVACAO);
        assertThat(output.relatoProblema()).isEqualTo("Barulho na suspensão dianteira");
        assertThat(output.veiculo()).isEqualTo(veiculo);
        assertThat(output.ultimoOrcamento()).isEqualTo(new OrcamentoResumoOutput(
                recente.getId(), recente.calcularValorTotal(), StatusOrcamento.PENDENTE,
                recente.getDataCriacao(), recente.getDataValidade()));
    }

    @ParameterizedTest
    @EnumSource(value = StatusOrcamento.class, names = {"APROVADO", "REJEITADO", "APROVADO_PARCIALMENTE"})
    void resumoMostraOStatusJaDecididoDoOrcamento(StatusOrcamento status) {
        when(repository.buscarPorId(ordemId)).thenReturn(Optional.of(ordemComOrcamentos(List.of(
                orcamento(status, Horario.agora().plusDays(1))))));
        when(consultorVeiculos.detalharPorId(veiculoId)).thenReturn(Optional.empty());

        assertThat(useCase().execute(ordemId, "12345678900", "ABC123").ultimoOrcamento().status()).isEqualTo(status);
    }

    @Test
    void orcamentoPendenteComValidadeVencidaApareceComoExpirado() {
        when(repository.buscarPorId(ordemId)).thenReturn(Optional.of(ordemComOrcamentos(List.of(
                orcamento(StatusOrcamento.PENDENTE, Horario.agora().minusMinutes(1))))));
        when(consultorVeiculos.detalharPorId(veiculoId)).thenReturn(Optional.empty());

        OrdemServicoClienteOutput output = useCase().execute(ordemId, "12345678900", "ABC123");

        assertThat(output.ultimoOrcamento().status()).isEqualTo(StatusOrcamento.EXPIRADO);
        assertThat(output.veiculo()).isNull();
    }

    @Test
    void semOrcamentosUltimoOrcamentoEhNulo() {
        when(repository.buscarPorId(ordemId)).thenReturn(Optional.of(ordemComOrcamentos(List.of())));
        when(consultorVeiculos.detalharPorId(veiculoId)).thenReturn(Optional.empty());

        assertThat(useCase().execute(ordemId, "12345678900", "ABC123").ultimoOrcamento()).isNull();
    }

    @Test
    void credenciaisInvalidasBarramAConsultaSemTocarNosDados() {
        doThrow(new RegraNegocioException("Credenciais inválidas"))
                .when(verificadorAcesso).validar(any(), any(), any());

        assertThatThrownBy(() -> useCase().execute(ordemId, "00000000000", "ERRADO"))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Credenciais inválidas");
        verifyNoInteractions(repository, consultorVeiculos);
    }

    @Test
    void ordemInexistenteViraRecursoNaoEncontrado() {
        doThrow(new RecursoNaoEncontradoException("Ordem de Serviço não encontrada."))
                .when(verificadorAcesso).validar(any(), any(), any());

        assertThatThrownBy(() -> useCase().execute(ordemId, "12345678900", "ABC123"))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Ordem de Serviço não encontrada.");
        verify(repository, never()).buscarPorId(any());
    }
}
