package br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.usecase;

import br.com.fiap.oficina_mecanica.compartilhado.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.oficina_mecanica.compartilhado.domain.exception.RegraNegocioException;
import br.com.fiap.oficina_mecanica.estoque.domain.ProdutoEstoque;
import br.com.fiap.oficina_mecanica.estoque.domain.ProdutoEstoqueRepository;
import br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.dto.AprovacaoInput;
import br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.dto.OrcamentoMapper;
import br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.dto.OrcamentoOutput;
import br.com.fiap.oficina_mecanica.ordemservico.domain.orcamento.ItemPeca;
import br.com.fiap.oficina_mecanica.ordemservico.domain.orcamento.Orcamento;
import br.com.fiap.oficina_mecanica.ordemservico.domain.orcamento.OrcamentoRepository;
import br.com.fiap.oficina_mecanica.ordemservico.domain.orcamento.StatusOrcamento;
import br.com.fiap.oficina_mecanica.ordemservico.domain.ordemservico.OrdemServico;
import br.com.fiap.oficina_mecanica.ordemservico.domain.ordemservico.OrdemServicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AprovarOrcamentoUseCase {

    private final OrcamentoRepository repository;
    private final OrdemServicoRepository ordemServicoRepository;
    private final ProdutoEstoqueRepository estoqueRepository;

    public AprovarOrcamentoUseCase(OrcamentoRepository repository, OrdemServicoRepository ordemServicoRepository, ProdutoEstoqueRepository estoqueRepository) {
        this.repository = repository;
        this.ordemServicoRepository = ordemServicoRepository;
        this.estoqueRepository = estoqueRepository;
    }

    @Transactional(noRollbackFor = RegraNegocioException.class)
    public OrcamentoOutput execute(UUID orcamentoId, AprovacaoInput input) {
        Orcamento orcamento = repository.buscarPorId(orcamentoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Orçamento não encontrado."));

        OrdemServico os = ordemServicoRepository.buscarPorId(orcamento.getOrdemServicoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ordem de Serviço não encontrada."));

        if (orcamento.verificarExpiracao()) {
            repository.salvar(orcamento);
            orcamento.getPecas().forEach(this::devolverReservaAoEstoque);

            os.recusarOrcamento();
            ordemServicoRepository.salvar(os);

            throw new RegraNegocioException("O orçamento está expirado e não pode ser aprovado.");
        }

        List<UUID> servicosAprovados = input != null ? input.servicosAprovados() : null;
        List<UUID> pecasAprovadas = input != null ? input.pecasAprovadas() : null;

        List<ItemPeca> pecasRecusadas = orcamento.aprovar(servicosAprovados, pecasAprovadas);

        Orcamento orcamentoSalvo = repository.salvar(orcamento);

        pecasRecusadas.forEach(this::devolverReservaAoEstoque);

        orcamento.getPecas().forEach(item -> {
            ProdutoEstoque produto = estoqueRepository.buscarPorId(item.produtoEstoqueId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Peça não encontrada: " + item.produtoEstoqueId()));

            produto.confirmarBaixa(item.quantidade());
            estoqueRepository.salvar(produto);
        });

        if (orcamento.getStatus() == StatusOrcamento.REJEITADO) {
            os.recusarOrcamento();
        } else {
            os.aprovarOrcamento();
        }
        ordemServicoRepository.salvar(os);

        return OrcamentoMapper.toOutput(orcamentoSalvo);
    }

    private void devolverReservaAoEstoque(ItemPeca item) {
        ProdutoEstoque produto = estoqueRepository.buscarPorId(item.produtoEstoqueId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Peça não encontrada: " + item.produtoEstoqueId()));
        produto.cancelarReserva(item.quantidade());
        estoqueRepository.salvar(produto);
    }
}