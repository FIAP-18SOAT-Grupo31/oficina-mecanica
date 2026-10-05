package br.com.fiap.oficina_mecanica.ordemservico.infrastructure.orcamento;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrcamentoJpaRepository extends JpaRepository<OrcamentoEntity, UUID> {

    Optional<OrcamentoEntity> findByOrdemServicoId(UUID ordemServicoId);
}