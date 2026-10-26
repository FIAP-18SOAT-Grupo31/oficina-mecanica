package br.com.fiap.oficina_mecanica.veiculo.infrastructure;

import br.com.fiap.oficina_mecanica.veiculo.domain.VeiculoDetalhe;
import br.com.fiap.oficina_mecanica.veiculo.domain.VeiculoRepository;
import br.com.fiap.oficina_mecanica.veiculo.domain.VeiculoResumo;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class VeiculoRepositoryAdapter implements VeiculoRepository {

    private final VeiculoJpaRepository jpaRepository;

    public VeiculoRepositoryAdapter(VeiculoJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public boolean existePorId(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public List<VeiculoResumo> buscarResumosPorIds(Collection<UUID> ids) {
        return jpaRepository.findAllById(ids).stream()
                .map(veiculo -> new VeiculoResumo(veiculo.getId(), veiculo.getPlaca(), veiculo.getMarca(), veiculo.getModelo()))
                .toList();
    }

    @Override
    public Optional<VeiculoDetalhe> buscarDetalhePorId(UUID id) {
        return jpaRepository.findById(id)
                .map(v -> new VeiculoDetalhe(v.getId(), v.getPlaca(), v.getMarca(), v.getModelo(), v.getAno()));
    }
}
