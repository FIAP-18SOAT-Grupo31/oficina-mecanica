package br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.usecase;

import br.com.fiap.oficina_mecanica.compartilhado.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.dto.OrcamentoOutput;
import br.com.fiap.oficina_mecanica.ordemservico.domain.orcamento.Orcamento;
import br.com.fiap.oficina_mecanica.ordemservico.domain.orcamento.OrcamentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class RejeitarOrcamentoClienteUseCase {

    private final OrcamentoRepository repository;
    private final VerificadorAcessoCliente verificadorAcesso;
    private final RejeitarOrcamentoUseCase rejeitarUseCaseBase;

    public RejeitarOrcamentoClienteUseCase(OrcamentoRepository repository,
                                           VerificadorAcessoCliente verificadorAcesso,
                                           RejeitarOrcamentoUseCase rejeitarUseCaseBase) {
        this.repository = repository;
        this.verificadorAcesso = verificadorAcesso;
        this.rejeitarUseCaseBase = rejeitarUseCaseBase;
    }

    @Transactional
    public OrcamentoOutput execute(UUID orcamentoId, String cpf, String codigoAcesso) {
        Orcamento orcamento = repository.buscarPorId(orcamentoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Orçamento não encontrado."));

        verificadorAcesso.validar(orcamento.getOrdemServicoId(), cpf, codigoAcesso);

        return rejeitarUseCaseBase.execute(orcamentoId);
    }
}