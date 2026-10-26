package br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.usecase;

import br.com.fiap.oficina_mecanica.compartilhado.application.PublicadorEventos;
import br.com.fiap.oficina_mecanica.compartilhado.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.oficina_mecanica.ordemservico.domain.evento.OrdemServicoFinalizada;
import br.com.fiap.oficina_mecanica.ordemservico.domain.ordemservico.OrdemServico;
import br.com.fiap.oficina_mecanica.ordemservico.domain.ordemservico.OrdemServicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class FinalizarOrdemServicoUseCase {

    private final OrdemServicoRepository repository;
    private final PublicadorEventos publicador;

    public FinalizarOrdemServicoUseCase(OrdemServicoRepository repository, PublicadorEventos publicador) {
        this.repository = repository;
        this.publicador = publicador;
    }

    @Transactional
    public void execute(UUID ordemServicoId) {
        OrdemServico ordemServico = repository.buscarPorId(ordemServicoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ordem de Serviço não encontrada."));

        ordemServico.finalizar();

        repository.salvar(ordemServico);
        publicador.publicar(new OrdemServicoFinalizada(ordemServico.getId(), ordemServico.getVeiculoId()));
    }
}
