package br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.usecase;

import br.com.fiap.oficina_mecanica.compartilhado.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.dto.OrcamentoOutput;
import br.com.fiap.oficina_mecanica.ordemservico.domain.orcamento.Orcamento;
import br.com.fiap.oficina_mecanica.ordemservico.domain.orcamento.OrcamentoRepository;
import br.com.fiap.oficina_mecanica.ordemservico.domain.ordemservico.OrdemServico;
import br.com.fiap.oficina_mecanica.ordemservico.domain.ordemservico.OrdemServicoRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class RejeitarOrcamentoClienteUseCase {

    private final OrcamentoRepository repository;
    private final VerificadorAcessoCliente verificadorAcesso;
    private final OrdemServicoRepository ordemServicoRepository;

    public RejeitarOrcamentoClienteUseCase(OrcamentoRepository repository, VerificadorAcessoCliente verificadorAcesso, OrdemServicoRepository ordemServicoRepository) {
        this.repository = repository;
        this.verificadorAcesso = verificadorAcesso;
        this.ordemServicoRepository = ordemServicoRepository;
    }

    @Transactional
    public OrcamentoOutput execute(UUID orcamentoId, String cpf, String codigoAcesso) {
        Orcamento orcamento = repository.buscarPorId(orcamentoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Orçamento não encontrado."));

        verificadorAcesso.validar(orcamento.getOrdemServicoId(), cpf, codigoAcesso);

        orcamento.rejeitar();

        Orcamento orcamentoSalvo = repository.salvar(orcamento);

        OrdemServico os = ordemServicoRepository.buscarPorId(orcamento.getOrdemServicoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ordem de Serviço não encontrada."));
        os.recusarOrcamento();
        ordemServicoRepository.salvar(os);

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