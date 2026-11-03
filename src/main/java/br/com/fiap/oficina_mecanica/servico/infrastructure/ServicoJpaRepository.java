package br.com.fiap.oficina_mecanica.servico.infrastructure;

import br.com.fiap.oficina_mecanica.servico.infrastructure.entity.ServicoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ServicoJpaRepository
        extends JpaRepository<ServicoEntity, UUID>, JpaSpecificationExecutor<ServicoEntity> {

    boolean existsByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCaseAndIdNot(String nome, UUID id);
}
