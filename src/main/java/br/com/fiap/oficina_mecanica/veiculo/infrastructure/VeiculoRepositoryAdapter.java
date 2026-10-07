package br.com.fiap.oficina_mecanica.veiculo.infrastructure;

import br.com.fiap.oficina_mecanica.veiculo.domain.VeiculoRepository;
import org.springframework.stereotype.Repository;

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
}
