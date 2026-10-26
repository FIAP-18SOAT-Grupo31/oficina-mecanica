package br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.usecase;

import br.com.fiap.oficina_mecanica.compartilhado.application.PublicadorEventos;
import br.com.fiap.oficina_mecanica.compartilhado.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.oficina_mecanica.estoque.domain.ProdutoEstoque;
import br.com.fiap.oficina_mecanica.estoque.domain.ProdutoEstoqueRepository;
import br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.dto.OrcamentoMapper;
import br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.dto.OrcamentoOutput;
import br.com.fiap.oficina_mecanica.ordemservico.domain.evento.OrdemServicoRecusada;
import br.com.fiap.oficina_mecanica.ordemservico.domain.orcamento.Orcamento;
import br.com.fiap.oficina_mecanica.ordemservico.domain.orcamento.OrcamentoRepository;
import br.com.fiap.oficina_mecanica.ordemservico.domain.ordemservico.OrdemServico;
import br.com.fiap.oficina_mecanica.ordemservico.domain.ordemservico.OrdemServicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class RejeitarOrcamentoUseCase {

    private final OrcamentoRepository repository;
    private final OrdemServicoRepository ordemServicoRepository;
    private final ProdutoEstoqueRepository estoqueRepository;
    private final PublicadorEventos publicador;

    public RejeitarOrcamentoUseCase(OrcamentoRepository repository, OrdemServicoRepository ordemServicoRepository,
                                    ProdutoEstoqueRepository estoqueRepository,
                                    PublicadorEventos publicador) {
        this.repository = repository;
        this.ordemServicoRepository = ordemServicoRepository;
        this.estoqueRepository = estoqueRepository;
        this.publicador = publicador;
    }

    @Transactional
    public OrcamentoOutput execute(UUID orcamentoId) {
        Orcamento orcamento = repository.buscarPorId(orcamentoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Orçamento não encontrado."));

        orcamento.rejeitar();
        Orcamento orcamentoSalvo = repository.salvar(orcamento);

        orcamento.getPecas().forEach(item -> {
            ProdutoEstoque produto = estoqueRepository.buscarPorId(item.produtoEstoqueId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Peça não encontrada no estoque: " + item.produtoEstoqueId()));

            produto.cancelarReserva(item.quantidade());
            estoqueRepository.salvar(produto);
        });

        OrdemServico os = ordemServicoRepository.buscarPorId(orcamento.getOrdemServicoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ordem de Serviço não encontrada."));
        os.recusarOrcamento();
        ordemServicoRepository.salvar(os);
        publicador.publicar(new OrdemServicoRecusada(os.getId()));

        return OrcamentoMapper.toOutput(orcamentoSalvo);
    }
}