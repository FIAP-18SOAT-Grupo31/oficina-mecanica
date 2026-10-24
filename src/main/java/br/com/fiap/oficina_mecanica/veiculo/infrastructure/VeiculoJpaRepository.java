package br.com.fiap.oficina_mecanica.veiculo.infrastructure;

import br.com.fiap.oficina_mecanica.veiculo.infrastructure.entity.VeiculoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface VeiculoJpaRepository
        extends JpaRepository<VeiculoEntity, UUID>, JpaSpecificationExecutor<VeiculoEntity> {

    boolean existsByClienteId(UUID clienteId);

    boolean existsByPlaca(String placa);

    boolean existsByPlacaAndIdNot(String placa, UUID id);
}
