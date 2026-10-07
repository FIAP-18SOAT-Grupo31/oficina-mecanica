package br.com.fiap.oficina_mecanica.ordemservico.infrastructure.ordemservico;

import br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.usecase.VerificadorVeiculo;
import br.com.fiap.oficina_mecanica.veiculo.domain.VeiculoRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class VerificadorVeiculoAdapter implements VerificadorVeiculo {

    private final VeiculoRepository veiculoRepository;

    public VerificadorVeiculoAdapter(VeiculoRepository veiculoRepository) {
        this.veiculoRepository = veiculoRepository;
    }

    @Override
    public boolean existe(UUID veiculoId) {
        return veiculoRepository.existePorId(veiculoId);
    }
}
