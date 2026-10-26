package br.com.fiap.oficina_mecanica.ordemservico.infrastructure.ordemservico;

import br.com.fiap.oficina_mecanica.ordemservico.domain.ordemservico.StatusOrdemServico;
import br.com.fiap.oficina_mecanica.ordemservico.infrastructure.ordemservico.entity.OrdemServicoEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface OrdemServicoJpaRepository extends JpaRepository<OrdemServicoEntity, UUID> {
    Page<OrdemServicoEntity> findByStatus(StatusOrdemServico status, Pageable pageable);
}
