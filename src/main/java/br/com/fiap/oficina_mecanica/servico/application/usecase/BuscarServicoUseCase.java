package br.com.fiap.oficina_mecanica.servico.application.usecase;

import br.com.fiap.oficina_mecanica.servico.application.dto.ServicoOutput;
import br.com.fiap.oficina_mecanica.servico.domain.ServicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class BuscarServicoUseCase {

    private final ServicoRepository repository;

    public BuscarServicoUseCase(ServicoRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public ServicoOutput execute(UUID id) {
        return repository.buscarPorId(id)
                .map(ServicoOutput::de)
                .orElseThrow(ServicoNaoEncontradoException::new);
    }
}
