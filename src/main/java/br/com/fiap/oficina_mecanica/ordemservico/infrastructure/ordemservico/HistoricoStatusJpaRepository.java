package br.com.fiap.oficina_mecanica.ordemservico.infrastructure.ordemservico;

import br.com.fiap.oficina_mecanica.ordemservico.infrastructure.ordemservico.entity.HistoricoStatusOrdemServicoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface HistoricoStatusJpaRepository extends JpaRepository<HistoricoStatusOrdemServicoEntity, UUID> {}
