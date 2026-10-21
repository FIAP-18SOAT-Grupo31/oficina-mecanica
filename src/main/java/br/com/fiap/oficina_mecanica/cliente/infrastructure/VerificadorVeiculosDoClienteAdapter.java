package br.com.fiap.oficina_mecanica.cliente.infrastructure;

import br.com.fiap.oficina_mecanica.cliente.application.usecase.VerificadorVeiculosDoCliente;
import br.com.fiap.oficina_mecanica.veiculo.domain.VeiculoRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class VerificadorVeiculosDoClienteAdapter implements VerificadorVeiculosDoCliente {

    private final VeiculoRepository veiculoRepository;

    public VerificadorVeiculosDoClienteAdapter(VeiculoRepository veiculoRepository) {
        this.veiculoRepository = veiculoRepository;
    }

    @Override
    public boolean possuiVeiculos(UUID clienteId) {
        return veiculoRepository.existePorClienteId(clienteId);
    }
}
