package br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.usecase;

import br.com.fiap.oficina_mecanica.compartilhado.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.dto.OrcamentoOutput;
import br.com.fiap.oficina_mecanica.ordemservico.domain.orcamento.Orcamento;
import br.com.fiap.oficina_mecanica.ordemservico.domain.orcamento.OrcamentoRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class BuscarOrcamentoUseCase {

    private final OrcamentoRepository repository;

    public BuscarOrcamentoUseCase(OrcamentoRepository repository) {
        this.repository = repository;
    }

    public OrcamentoOutput execute(UUID id) {
        Orcamento orcamento = repository.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Orçamento não encontrado com o ID informado."));

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