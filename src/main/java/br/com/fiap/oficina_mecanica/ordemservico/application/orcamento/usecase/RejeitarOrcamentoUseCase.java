package br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.usecase;

import br.com.fiap.oficina_mecanica.compartilhado.domain.exception.RecursoNaoEncontradoException;
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
public class RejeitarOrcamentoUseCase {

    private final OrcamentoRepository repository;
    private final OrdemServicoRepository ordemServicoRepository;

    public RejeitarOrcamentoUseCase(OrcamentoRepository repository, OrdemServicoRepository ordemServicoRepository) {
        this.repository = repository;
        this.ordemServicoRepository = ordemServicoRepository;
    }

    @Transactional
    public OrcamentoOutput execute(UUID orcamentoId) {
        Orcamento orcamento = repository.buscarPorId(orcamentoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Orçamento não encontrado."));

        orcamento.rejeitar();
        Orcamento orcamentoSalvo = repository.salvar(orcamento);

        OrdemServico os = ordemServicoRepository.buscarPorId(orcamento.getOrdemServicoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ordem de Serviço não encontrada."));
        os.recusarOrcamento();
        ordemServicoRepository.salvar(os);

        return OrcamentoMapper.toOutput(orcamentoSalvo);
    }
}