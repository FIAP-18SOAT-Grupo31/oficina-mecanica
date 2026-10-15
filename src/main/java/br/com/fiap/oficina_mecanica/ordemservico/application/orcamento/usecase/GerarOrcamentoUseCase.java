package br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.usecase;

import br.com.fiap.oficina_mecanica.compartilhado.domain.Horario;
import br.com.fiap.oficina_mecanica.compartilhado.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.oficina_mecanica.compartilhado.domain.exception.RegraNegocioException;
import br.com.fiap.oficina_mecanica.estoque.domain.ProdutoEstoque;
import br.com.fiap.oficina_mecanica.estoque.domain.ProdutoEstoqueRepository;
import br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.dto.GerarOrcamentoInput;
import br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.dto.OrcamentoMapper;
import br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.dto.OrcamentoOutput;
import br.com.fiap.oficina_mecanica.ordemservico.domain.orcamento.ItemPeca;
import br.com.fiap.oficina_mecanica.ordemservico.domain.orcamento.ItemServico;
import br.com.fiap.oficina_mecanica.ordemservico.domain.orcamento.Orcamento;
import br.com.fiap.oficina_mecanica.ordemservico.domain.orcamento.OrcamentoRepository;
import br.com.fiap.oficina_mecanica.ordemservico.domain.ordemservico.OrdemServico;
import br.com.fiap.oficina_mecanica.ordemservico.domain.ordemservico.OrdemServicoRepository;
import br.com.fiap.oficina_mecanica.ordemservico.domain.ordemservico.StatusOrdemServico;
import com.github.f4b6a3.uuid.UuidCreator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class GerarOrcamentoUseCase {

    private final OrcamentoRepository repository;
    private final NotificadorOrcamento notificador;
    private final OrdemServicoRepository ordemServicoRepository;
    private final ProdutoEstoqueRepository estoqueRepository;

    public GerarOrcamentoUseCase(OrcamentoRepository repository, NotificadorOrcamento notificador,
                                 OrdemServicoRepository ordemServicoRepository,
                                 ProdutoEstoqueRepository estoqueRepository) {
        this.repository = repository;
        this.notificador = notificador;
        this.ordemServicoRepository = ordemServicoRepository;
        this.estoqueRepository = estoqueRepository;
    }

    @Transactional
    public OrcamentoOutput execute(GerarOrcamentoInput input) {
        if (input.dataValidade() == null || input.dataValidade().isBefore(Horario.agora())) {
            throw new RegraNegocioException("A data de validade deve ser informada e estar no futuro.");
        }

        OrdemServico os = ordemServicoRepository.buscarPorId(input.ordemServicoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ordem de Serviço não encontrada."));

        if (os.getStatus() == StatusOrdemServico.RECEBIDA) {
            os.iniciarDiagnostico();
        }

        UUID orcamentoId = UuidCreator.getTimeOrderedEpoch();
        Orcamento orcamento = new Orcamento(orcamentoId, input.ordemServicoId(), input.dataValidade());

        if (input.servicos() != null) {
            input.servicos().forEach(s ->
                    orcamento.adicionarServico(new ItemServico(s.catalogoServicoId(), s.descricao(), s.valorMaoDeObra()))
            );
        }

        if (input.pecas() != null) {
            input.pecas().forEach(p -> {
                ProdutoEstoque produto = estoqueRepository.buscarPorId(p.produtoEstoqueId())
                        .orElseThrow(() -> new RecursoNaoEncontradoException("Peça não encontrada no estoque: " + p.produtoEstoqueId()));

                produto.reservar(p.quantidade());
                estoqueRepository.salvar(produto);

                orcamento.adicionarPeca(new ItemPeca(p.produtoEstoqueId(), produto.getNomePeca(), p.quantidade(), produto.getValorUnitario()));
            });
        }

        os.anexarOrcamento(orcamento);

        Orcamento orcamentoSalvo = repository.salvar(orcamento);
        ordemServicoRepository.salvar(os);

        notificador.notificarOrcamentoGerado(orcamentoSalvo);

        return OrcamentoMapper.toOutput(orcamentoSalvo);
    }
}