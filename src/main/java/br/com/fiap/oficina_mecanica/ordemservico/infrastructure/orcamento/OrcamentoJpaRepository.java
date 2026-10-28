package br.com.fiap.oficina_mecanica.ordemservico.infrastructure.orcamento;

import br.com.fiap.oficina_mecanica.ordemservico.infrastructure.orcamento.entity.OrcamentoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrcamentoJpaRepository extends JpaRepository<OrcamentoEntity, UUID> {

    List<OrcamentoEntity> findByOrdemServicoIdOrderByDataCriacao(UUID ordemServicoId);
}