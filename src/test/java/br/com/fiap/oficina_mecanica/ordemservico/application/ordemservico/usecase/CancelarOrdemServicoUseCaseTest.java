package br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.usecase;

import br.com.fiap.oficina_mecanica.compartilhado.application.PublicadorEventos;
import br.com.fiap.oficina_mecanica.compartilhado.domain.Horario;
import br.com.fiap.oficina_mecanica.compartilhado.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.oficina_mecanica.compartilhado.domain.exception.RegraNegocioException;
import br.com.fiap.oficina_mecanica.estoque.domain.ProdutoEstoque;
import br.com.fiap.oficina_mecanica.estoque.domain.ProdutoEstoqueRepository;
import br.com.fiap.oficina_mecanica.ordemservico.domain.evento.OrdemServicoCancelada;
import br.com.fiap.oficina_mecanica.ordemservico.domain.orcamento.ItemPeca;
import br.com.fiap.oficina_mecanica.ordemservico.domain.orcamento.Orcamento;
import br.com.fiap.oficina_mecanica.ordemservico.domain.orcamento.OrcamentoRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CancelarOrdemServicoUseCaseTest {

    @Mock
    private OrdemServicoRepository repository;

    @Mock
    private OrcamentoRepository orcamentoRepository;

    @Mock
    private ProdutoEstoqueRepository estoqueRepository;

    @Mock
    private PublicadorEventos publicador;

    @InjectMocks
    private CancelarOrdemServicoUseCase useCase;

    private final UUID ordemId = UUID.randomUUID();

    private OrdemServico ordem(StatusOrdemServico status, List<Orcamento> orcamentos) {
        return OrdemServico.builder()
                .id(ordemId).clienteId(UUID.randomUUID()).veiculoId(UUID.randomUUID())
                .relatoProblema("Barulho na suspensão dianteira").status(status).dataAbertura(Horario.agora())
                .orcamentos(new ArrayList<>(orcamentos))
                .build();
    }

    private Orcamento orcamento(StatusOrcamento status, List<ItemPeca> pecas) {
        return new Orcamento(UUID.randomUUID(), ordemId, Horario.agora(), Horario.agora().plusDays(1), status,
                List.of(), pecas);
    }

    @Test
    void cancelaOrdemRecebidaESalva() {
        OrdemServico ordem = ordem(StatusOrdemServico.RECEBIDA, List.of());
        when(repository.buscarPorId(ordemId)).thenReturn(Optional.of(ordem));

        useCase.execute(ordemId);

        assertThat(ordem.getStatus()).isEqualTo(StatusOrdemServico.CANCELADA);
        verify(repository).salvar(ordem);
        verify(publicador).publicar(new OrdemServicoCancelada(ordemId));
        verifyNoInteractions(orcamentoRepository, estoqueRepository);
    }

    @Test
    void cancelarComOrcamentoPendenteRejeitaOOrcamentoELiberaAsReservas() {
        UUID produtoId = UUID.randomUUID();
        ProdutoEstoque produto = new ProdutoEstoque(produtoId, "Filtro de óleo", 10, new BigDecimal("40.00"));
        produto.reservar(3);
        Orcamento pendente = orcamento(StatusOrcamento.PENDENTE,
                List.of(new ItemPeca(produtoId, "Filtro de óleo", 3, new BigDecimal("40.00"))));
        OrdemServico ordem = ordem(StatusOrdemServico.AGUARDANDO_APROVACAO, List.of(pendente));
        when(repository.buscarPorId(ordemId)).thenReturn(Optional.of(ordem));
        when(estoqueRepository.buscarPorId(produtoId)).thenReturn(Optional.of(produto));

        useCase.execute(ordemId);

        assertThat(ordem.getStatus()).isEqualTo(StatusOrdemServico.CANCELADA);
        assertThat(pendente.getStatus()).isEqualTo(StatusOrcamento.REJEITADO);
        assertThat(produto.getQuantidadeReservada()).isZero();
        assertThat(produto.getQuantidadeDisponivel()).isEqualTo(10);
        verify(orcamentoRepository).salvar(pendente);
        verify(estoqueRepository).salvar(produto);
        verify(repository).salvar(ordem);
        verify(publicador).publicar(new OrdemServicoCancelada(ordemId));
    }

    @Test
    void orcamentosJaDecididosNaoSaoTocados() {
        Orcamento expirado = orcamento(StatusOrcamento.EXPIRADO, List.of());
        OrdemServico ordem = ordem(StatusOrdemServico.AGUARDANDO_APROVACAO, List.of(expirado));
        when(repository.buscarPorId(ordemId)).thenReturn(Optional.of(ordem));

        useCase.execute(ordemId);

        assertThat(expirado.getStatus()).isEqualTo(StatusOrcamento.EXPIRADO);
        verify(orcamentoRepository, never()).salvar(any());
        verifyNoInteractions(estoqueRepository);
    }

    @Test
    void ordemInexistenteViraRecursoNaoEncontrado() {
        when(repository.buscarPorId(ordemId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(ordemId))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Ordem de Serviço não encontrada.");
        verifyNoInteractions(publicador);
    }

    @Test
    void ordemJaAprovadaNaoPodeSerCanceladaENadaEhAlterado() {
        Orcamento aprovado = orcamento(StatusOrcamento.APROVADO, List.of());
        when(repository.buscarPorId(ordemId))
                .thenReturn(Optional.of(ordem(StatusOrdemServico.APROVADA, List.of(aprovado))));

        assertThatThrownBy(() -> useCase.execute(ordemId)).isInstanceOf(RegraNegocioException.class);

        verify(repository, never()).salvar(any());
        verifyNoInteractions(orcamentoRepository, estoqueRepository, publicador);
    }

    @Test
    void ordemJaCanceladaNaoPodeSerCanceladaDeNovo() {
        when(repository.buscarPorId(ordemId))
                .thenReturn(Optional.of(ordem(StatusOrdemServico.CANCELADA, List.of())));

        assertThatThrownBy(() -> useCase.execute(ordemId)).isInstanceOf(RegraNegocioException.class);
        verify(repository, never()).salvar(any());
        verifyNoInteractions(publicador);
    }

    @Test
    void pecaInexistenteNoEstoqueInterrompeOCancelamento() {
        UUID produtoId = UUID.randomUUID();
        Orcamento pendente = orcamento(StatusOrcamento.PENDENTE,
                List.of(new ItemPeca(produtoId, "Filtro", 1, new BigDecimal("40.00"))));
        when(repository.buscarPorId(ordemId))
                .thenReturn(Optional.of(ordem(StatusOrdemServico.AGUARDANDO_APROVACAO, List.of(pendente))));
        when(estoqueRepository.buscarPorId(produtoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(ordemId)).isInstanceOf(RecursoNaoEncontradoException.class);
        verify(repository, never()).salvar(any());
        verifyNoInteractions(publicador);
    }
}
