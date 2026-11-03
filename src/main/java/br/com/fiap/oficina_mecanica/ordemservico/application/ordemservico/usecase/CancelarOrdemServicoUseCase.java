package br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.usecase;

import br.com.fiap.oficina_mecanica.compartilhado.application.PublicadorEventos;
import br.com.fiap.oficina_mecanica.compartilhado.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.oficina_mecanica.estoque.domain.ProdutoEstoque;
import br.com.fiap.oficina_mecanica.estoque.domain.ProdutoEstoqueRepository;
import br.com.fiap.oficina_mecanica.ordemservico.domain.evento.OrdemServicoCancelada;
import br.com.fiap.oficina_mecanica.ordemservico.domain.orcamento.Orcamento;
import br.com.fiap.oficina_mecanica.ordemservico.domain.orcamento.OrcamentoRepository;
import br.com.fiap.oficina_mecanica.ordemservico.domain.orcamento.StatusOrcamento;
import br.com.fiap.oficina_mecanica.ordemservico.domain.ordemservico.OrdemServico;
import br.com.fiap.oficina_mecanica.ordemservico.domain.ordemservico.OrdemServicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CancelarOrdemServicoUseCase {

    private final OrdemServicoRepository repository;
    private final OrcamentoRepository orcamentoRepository;
    private final ProdutoEstoqueRepository estoqueRepository;
    private final PublicadorEventos publicador;

    public CancelarOrdemServicoUseCase(OrdemServicoRepository repository, OrcamentoRepository orcamentoRepository,
                                       ProdutoEstoqueRepository estoqueRepository,
                                       PublicadorEventos publicador) {
        this.repository = repository;
        this.orcamentoRepository = orcamentoRepository;
        this.estoqueRepository = estoqueRepository;
        this.publicador = publicador;
    }

    @Transactional
    public void execute(UUID ordemServicoId) {
        OrdemServico ordemServico = repository.buscarPorId(ordemServicoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ordem de Serviço não encontrada."));

        ordemServico.cancelar();

        ordemServico.getOrcamentos().stream()
                .filter(orcamento -> orcamento.getStatus() == StatusOrcamento.PENDENTE)
                .forEach(this::rejeitarLiberandoReservas);

        repository.salvar(ordemServico);
        publicador.publicar(new OrdemServicoCancelada(ordemServico.getId()));
    }

    private void rejeitarLiberandoReservas(Orcamento orcamento) {
        orcamento.rejeitar();
        orcamentoRepository.salvar(orcamento);

        orcamento.getPecas().forEach(item -> {
            ProdutoEstoque produto = estoqueRepository.buscarPorId(item.produtoEstoqueId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException(
                            "Peça não encontrada no estoque: " + item.produtoEstoqueId()));
            produto.cancelarReserva(item.quantidade());
            estoqueRepository.salvar(produto);
        });
    }
}
