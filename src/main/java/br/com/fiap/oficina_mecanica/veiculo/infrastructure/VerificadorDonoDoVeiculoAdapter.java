package br.com.fiap.oficina_mecanica.veiculo.infrastructure;

import br.com.fiap.oficina_mecanica.cliente.domain.ClienteRepository;
import br.com.fiap.oficina_mecanica.veiculo.application.usecase.VerificadorDonoDoVeiculo;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class VerificadorDonoDoVeiculoAdapter implements VerificadorDonoDoVeiculo {

    private final ClienteRepository clienteRepository;

    public VerificadorDonoDoVeiculoAdapter(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Override
    public boolean existe(UUID clienteId) {
        return clienteRepository.existePorId(clienteId);
    }
}
