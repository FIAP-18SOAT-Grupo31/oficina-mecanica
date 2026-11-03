package br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.usecase;

import br.com.fiap.oficina_mecanica.compartilhado.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.oficina_mecanica.ordemservico.domain.ordemservico.OrdemServico;
import br.com.fiap.oficina_mecanica.ordemservico.domain.ordemservico.OrdemServicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class IniciarExecucaoOrdemServicoUseCase {

    private final OrdemServicoRepository repository;

    public IniciarExecucaoOrdemServicoUseCase(OrdemServicoRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void execute(UUID ordemServicoId) {
        OrdemServico ordemServico = repository.buscarPorId(ordemServicoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ordem de Serviço não encontrada."));

        ordemServico.iniciarExecucao();

        repository.salvar(ordemServico);
    }
}
