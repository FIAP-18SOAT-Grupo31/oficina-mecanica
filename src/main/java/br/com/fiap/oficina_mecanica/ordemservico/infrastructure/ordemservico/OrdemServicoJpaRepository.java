package br.com.fiap.oficina_mecanica.ordemservico.infrastructure.ordemservico;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrdemServicoJpaRepository extends JpaRepository<OrdemServicoEntity, UUID> {
    
    Optional<OrdemServicoEntity> findById(UUID id);
}
