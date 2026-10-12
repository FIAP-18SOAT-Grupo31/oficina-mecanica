package br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.usecase;

import br.com.fiap.oficina_mecanica.compartilhado.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.dto.GerarOrcamentoInput;
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

    public GerarOrcamentoUseCase(OrcamentoRepository repository, NotificadorOrcamento notificador, OrdemServicoRepository ordemServicoRepository) {
        this.repository = repository;
        this.notificador = notificador;
        this.ordemServicoRepository = ordemServicoRepository;
    }

    @Transactional
    public OrcamentoOutput execute(GerarOrcamentoInput input) {
        UUID orcamentoId = UuidCreator.getTimeOrderedEpoch();

        Orcamento orcamento = new Orcamento(orcamentoId, input.ordemServicoId(), input.dataValidade());

        if (input.servicos() != null) {
            input.servicos().forEach(s ->
                    orcamento.adicionarServico(new ItemServico(s.catalogoServicoId(), s.descricao(), s.valorMaoDeObra()))
            );
        }

        if (input.pecas() != null) {
            input.pecas().forEach(p ->
                    orcamento.adicionarPeca(new ItemPeca(p.produtoEstoqueId(), p.nomePeca(), p.quantidade(), p.valorUnitario()))
            );
        }

        Orcamento orcamentoSalvo = repository.salvar(orcamento);

        OrdemServico os = ordemServicoRepository.buscarPorId(orcamento.getOrdemServicoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ordem de Serviço não encontrada."));

        if (os.getStatus() == StatusOrdemServico.RECEBIDA) {
            os.iniciarDiagnostico();
        }

        os.anexarOrcamento(orcamentoSalvo);
        ordemServicoRepository.salvar(os);

        notificador.notificarOrcamentoGerado(orcamentoSalvo);

        return new OrcamentoOutput(
                orcamentoSalvo.getId(),
                orcamentoSalvo.getOrdemServicoId(),
                orcamentoSalvo.calcularValorTotal(),
                orcamentoSalvo.getStatus(),
                orcamentoSalvo.getDataCriacao(),
                orcamentoSalvo.getDataValidade()
        );
    }
}