package br.com.fiap.oficina_mecanica.cliente.infrastructure;

import br.com.fiap.oficina_mecanica.cliente.infrastructure.entity.ClienteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ClienteJpaRepository
        extends JpaRepository<ClienteEntity, UUID>, JpaSpecificationExecutor<ClienteEntity> {

    boolean existsByDocumento(String documento);

    boolean existsByDocumentoAndIdNot(String documento, UUID id);
}
