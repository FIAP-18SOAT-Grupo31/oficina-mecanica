package br.com.fiap.oficina_mecanica.ordemservico.infrastructure.ordemservico;

import br.com.fiap.oficina_mecanica.cliente.domain.ClienteRepository;
import br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.usecase.VerificadorCliente;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class VerificadorClienteAdapter implements VerificadorCliente {

    private final ClienteRepository clienteRepository;

    public VerificadorClienteAdapter(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Override
    public boolean existe(UUID clienteId) {
        return clienteRepository.existePorId(clienteId);
    }
}
