package br.com.fiap.oficina_mecanica.veiculo.infrastructure;

import br.com.fiap.oficina_mecanica.ordemservico.domain.ordemservico.OrdemServicoRepository;
import br.com.fiap.oficina_mecanica.veiculo.application.usecase.VerificadorOrdensServicoDoVeiculo;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class VerificadorOrdensServicoDoVeiculoAdapter implements VerificadorOrdensServicoDoVeiculo {

    private final OrdemServicoRepository ordemServicoRepository;

    public VerificadorOrdensServicoDoVeiculoAdapter(OrdemServicoRepository ordemServicoRepository) {
        this.ordemServicoRepository = ordemServicoRepository;
    }

    @Override
    public boolean possuiOrdensServico(UUID veiculoId) {
        return ordemServicoRepository.existePorVeiculoId(veiculoId);
    }
}
