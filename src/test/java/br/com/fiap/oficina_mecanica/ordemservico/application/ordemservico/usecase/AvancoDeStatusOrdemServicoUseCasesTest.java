package br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.usecase;

import br.com.fiap.oficina_mecanica.compartilhado.application.PublicadorEventos;
import br.com.fiap.oficina_mecanica.compartilhado.domain.Horario;
import br.com.fiap.oficina_mecanica.compartilhado.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.oficina_mecanica.compartilhado.domain.exception.RegraNegocioException;
import br.com.fiap.oficina_mecanica.ordemservico.domain.evento.OrdemServicoFinalizada;
import br.com.fiap.oficina_mecanica.ordemservico.domain.ordemservico.OrdemServico;
import br.com.fiap.oficina_mecanica.ordemservico.domain.ordemservico.OrdemServicoRepository;
import br.com.fiap.oficina_mecanica.ordemservico.domain.ordemservico.StatusOrdemServico;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AvancoDeStatusOrdemServicoUseCasesTest {

    @Mock
    private OrdemServicoRepository repository;

    @Mock
    private PublicadorEventos publicador;

    private final UUID ordemId = UUID.randomUUID();

    private OrdemServico ordemComStatus(StatusOrdemServico status) {
        return OrdemServico.builder()
                .id(ordemId).clienteId(UUID.randomUUID()).veiculoId(UUID.randomUUID())
                .relatoProblema("Barulho na suspensão dianteira").status(status).dataAbertura(Horario.agora())
                .orcamentos(new ArrayList<>())
                .build();
    }

    private void ordemExistente(StatusOrdemServico status) {
        when(repository.buscarPorId(ordemId)).thenReturn(Optional.of(ordemComStatus(status)));
    }

    @Test
    void iniciaDiagnosticoEPersiste() {
        OrdemServico ordem = ordemComStatus(StatusOrdemServico.RECEBIDA);
        when(repository.buscarPorId(ordemId)).thenReturn(Optional.of(ordem));

        new IniciarDiagnosticoOrdemServicoUseCase(repository).execute(ordemId);

        assertThat(ordem.getStatus()).isEqualTo(StatusOrdemServico.EM_DIAGNOSTICO);
        verify(repository).salvar(ordem);
    }

    @Test
    void iniciarDiagnosticoForaDeOrdemRecebidaFalhaSemPersistir() {
        ordemExistente(StatusOrdemServico.EM_DIAGNOSTICO);

        assertThatThrownBy(() -> new IniciarDiagnosticoOrdemServicoUseCase(repository).execute(ordemId))
                .isInstanceOf(RegraNegocioException.class);
        verify(repository, never()).salvar(any());
    }

    @Test
    void iniciarDiagnosticoDeOrdemInexistenteViraRecursoNaoEncontrado() {
        when(repository.buscarPorId(ordemId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> new IniciarDiagnosticoOrdemServicoUseCase(repository).execute(ordemId))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Ordem de Serviço não encontrada.");
        verify(repository, never()).salvar(any());
    }

    @Test
    void iniciaExecucaoEPersiste() {
        OrdemServico ordem = ordemComStatus(StatusOrdemServico.APROVADA);
        when(repository.buscarPorId(ordemId)).thenReturn(Optional.of(ordem));

        new IniciarExecucaoOrdemServicoUseCase(repository).execute(ordemId);

        assertThat(ordem.getStatus()).isEqualTo(StatusOrdemServico.EM_EXECUCAO);
        verify(repository).salvar(ordem);
    }

    @Test
    void iniciarExecucaoSemAprovacaoFalhaSemPersistir() {
        ordemExistente(StatusOrdemServico.AGUARDANDO_APROVACAO);

        assertThatThrownBy(() -> new IniciarExecucaoOrdemServicoUseCase(repository).execute(ordemId))
                .isInstanceOf(RegraNegocioException.class);
        verify(repository, never()).salvar(any());
    }

    @Test
    void iniciarExecucaoDeOrdemInexistenteViraRecursoNaoEncontrado() {
        when(repository.buscarPorId(ordemId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> new IniciarExecucaoOrdemServicoUseCase(repository).execute(ordemId))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Ordem de Serviço não encontrada.");
        verify(repository, never()).salvar(any());
    }

    @Test
    void finalizaERegistraADataDeConclusaoEPublicaOEvento() {
        OrdemServico ordem = ordemComStatus(StatusOrdemServico.EM_EXECUCAO);
        when(repository.buscarPorId(ordemId)).thenReturn(Optional.of(ordem));

        new FinalizarOrdemServicoUseCase(repository, publicador).execute(ordemId);

        assertThat(ordem.getStatus()).isEqualTo(StatusOrdemServico.FINALIZADA);
        assertThat(ordem.getDataConclusao()).isNotNull();
        InOrder ordemDeChamadas = inOrder(repository, publicador);
        ordemDeChamadas.verify(repository).salvar(ordem);
        ordemDeChamadas.verify(publicador).publicar(new OrdemServicoFinalizada(ordemId, ordem.getVeiculoId()));
    }

    @Test
    void finalizarSemEstarEmExecucaoFalhaSemPersistirEPublicar() {
        ordemExistente(StatusOrdemServico.APROVADA);

        assertThatThrownBy(() -> new FinalizarOrdemServicoUseCase(repository, publicador).execute(ordemId))
                .isInstanceOf(RegraNegocioException.class);
        verify(repository, never()).salvar(any());
        verifyNoInteractions(publicador);
    }

    @Test
    void finalizarOrdemInexistenteViraRecursoNaoEncontradoSemPublicar() {
        when(repository.buscarPorId(ordemId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> new FinalizarOrdemServicoUseCase(repository, publicador).execute(ordemId))
                .isInstanceOf(RecursoNaoEncontradoException.class);
        verify(repository, never()).salvar(any());
        verifyNoInteractions(publicador);
    }

    @Test
    void entregaOrdemFinalizadaEPersiste() {
        OrdemServico ordem = ordemComStatus(StatusOrdemServico.FINALIZADA);
        when(repository.buscarPorId(ordemId)).thenReturn(Optional.of(ordem));

        new EntregarOrdemServicoUseCase(repository).execute(ordemId);

        assertThat(ordem.getStatus()).isEqualTo(StatusOrdemServico.ENTREGUE);
        verify(repository).salvar(ordem);
    }

    @Test
    void entregarOrdemNaoFinalizadaFalhaSemPersistir() {
        ordemExistente(StatusOrdemServico.EM_EXECUCAO);

        assertThatThrownBy(() -> new EntregarOrdemServicoUseCase(repository).execute(ordemId))
                .isInstanceOf(RegraNegocioException.class);
        verify(repository, never()).salvar(any());
    }

    @Test
    void entregarOrdemJaEntregueFalha() {
        ordemExistente(StatusOrdemServico.ENTREGUE);

        assertThatThrownBy(() -> new EntregarOrdemServicoUseCase(repository).execute(ordemId))
                .isInstanceOf(RegraNegocioException.class);
        verify(repository, never()).salvar(any());
    }

    @Test
    void entregarOrdemInexistenteViraRecursoNaoEncontrado() {
        when(repository.buscarPorId(ordemId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> new EntregarOrdemServicoUseCase(repository).execute(ordemId))
                .isInstanceOf(RecursoNaoEncontradoException.class);
        verify(repository, never()).salvar(any());
    }
}
