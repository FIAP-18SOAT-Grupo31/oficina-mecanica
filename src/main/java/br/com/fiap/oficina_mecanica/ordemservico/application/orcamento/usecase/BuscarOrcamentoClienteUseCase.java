package br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.usecase;

import br.com.fiap.oficina_mecanica.compartilhado.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.dto.OrcamentoOutput;
import br.com.fiap.oficina_mecanica.ordemservico.domain.orcamento.Orcamento;
import br.com.fiap.oficina_mecanica.ordemservico.domain.orcamento.OrcamentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class BuscarOrcamentoClienteUseCase {

    private final OrcamentoRepository repository;
    private final VerificadorAcessoCliente verificadorAcesso;

    public BuscarOrcamentoClienteUseCase(OrcamentoRepository repository, VerificadorAcessoCliente verificadorAcesso) {
        this.repository = repository;
        this.verificadorAcesso = verificadorAcesso;
    }

    @Transactional(readOnly = true)
    public OrcamentoOutput execute(UUID orcamentoId, String cpf, String codigoAcesso) {
        Orcamento orcamento = repository.buscarPorId(orcamentoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Orçamento não encontrado."));

        verificadorAcesso.validar(orcamento.getOrdemServicoId(), cpf, codigoAcesso);

        return new OrcamentoOutput(
                orcamento.getId(),
                orcamento.getOrdemServicoId(),
                orcamento.calcularValorTotal(),
                orcamento.getStatus(),
                orcamento.getDataCriacao(),
                orcamento.getDataValidade()
        );
    }
}