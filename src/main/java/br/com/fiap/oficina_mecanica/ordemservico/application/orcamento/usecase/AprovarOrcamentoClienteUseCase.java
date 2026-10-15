package br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.usecase;

import br.com.fiap.oficina_mecanica.compartilhado.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.oficina_mecanica.estoque.domain.ProdutoEstoque;
import br.com.fiap.oficina_mecanica.estoque.domain.ProdutoEstoqueRepository;
import br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.dto.AprovacaoClienteInput;
import br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.dto.OrcamentoMapper;
import br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.dto.OrcamentoOutput;
import br.com.fiap.oficina_mecanica.ordemservico.domain.orcamento.Orcamento;
import br.com.fiap.oficina_mecanica.ordemservico.domain.orcamento.OrcamentoRepository;
import br.com.fiap.oficina_mecanica.ordemservico.domain.ordemservico.OrdemServico;
import br.com.fiap.oficina_mecanica.ordemservico.domain.ordemservico.OrdemServicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AprovarOrcamentoClienteUseCase {

    private final OrcamentoRepository repository;
    private final VerificadorAcessoCliente verificadorAcesso;
    private final OrdemServicoRepository ordemServicoRepository;
    private final ProdutoEstoqueRepository estoqueRepository;

    public AprovarOrcamentoClienteUseCase(OrcamentoRepository repository, VerificadorAcessoCliente verificadorAcesso,
                                          OrdemServicoRepository ordemServicoRepository,
                                          ProdutoEstoqueRepository estoqueRepository) {
        this.repository = repository;
        this.verificadorAcesso = verificadorAcesso;
        this.ordemServicoRepository = ordemServicoRepository;
        this.estoqueRepository = estoqueRepository;
    }

    @Transactional
    public OrcamentoOutput execute(UUID orcamentoId, AprovacaoClienteInput input) {
        Orcamento orcamento = repository.buscarPorId(orcamentoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Orçamento não encontrado."));

        verificadorAcesso.validar(orcamento.getOrdemServicoId(), input.cpf(), input.codigoAcesso());

        orcamento.aprovar(input.servicosAprovados(), input.pecasAprovadas());

        Orcamento orcamentoSalvo = repository.salvar(orcamento);

        orcamento.getPecas().forEach(item -> {
            ProdutoEstoque produto = estoqueRepository.buscarPorId(item.produtoEstoqueId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Peça não encontrada no estoque: " + item.produtoEstoqueId()));

            produto.confirmarBaixa(item.quantidade());
            estoqueRepository.salvar(produto);
        });

        OrdemServico os = ordemServicoRepository.buscarPorId(orcamento.getOrdemServicoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ordem de Serviço não encontrada."));
        os.aprovarOrcamento();
        ordemServicoRepository.salvar(os);

        return OrcamentoMapper.toOutput(orcamentoSalvo);
    }
}