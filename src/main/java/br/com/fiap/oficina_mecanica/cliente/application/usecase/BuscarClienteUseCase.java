package br.com.fiap.oficina_mecanica.cliente.application.usecase;

import br.com.fiap.oficina_mecanica.cliente.application.dto.ClienteOutput;
import br.com.fiap.oficina_mecanica.cliente.domain.ClienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class BuscarClienteUseCase {

    private final ClienteRepository repository;

    public BuscarClienteUseCase(ClienteRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public ClienteOutput execute(UUID id) {
        return repository.buscarPorId(id)
                .map(ClienteOutput::de)
                .orElseThrow(ClienteNaoEncontradoException::new);
    }
}
