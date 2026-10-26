package br.com.fiap.oficina_mecanica.ordemservico.infrastructure.ordemservico;

import br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.dto.VeiculoResumoOutput;
import br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.usecase.ConsultorVeiculos;
import br.com.fiap.oficina_mecanica.veiculo.domain.VeiculoRepository;
import br.com.fiap.oficina_mecanica.veiculo.domain.VeiculoResumo;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ConsultorVeiculosAdapter implements ConsultorVeiculos {

    private final VeiculoRepository veiculoRepository;

    public ConsultorVeiculosAdapter(VeiculoRepository veiculoRepository) {
        this.veiculoRepository = veiculoRepository;
    }

    @Override
    public Map<UUID, VeiculoResumoOutput> resumirPorIds(Collection<UUID> veiculoIds) {
        return veiculoRepository.buscarResumosPorIds(veiculoIds).stream()
                .collect(Collectors.toMap(VeiculoResumo::id, paraOutput(), (a, b) -> a));
    }

    private static Function<VeiculoResumo, VeiculoResumoOutput> paraOutput() {
        return veiculo -> new VeiculoResumoOutput(veiculo.id(), veiculo.placa(), veiculo.marca(), veiculo.modelo());
    }
}
