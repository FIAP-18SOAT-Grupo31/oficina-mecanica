package br.com.fiap.oficina_mecanica.cliente.infrastructure;

import br.com.fiap.oficina_mecanica.cliente.domain.ClienteRepository;
import br.com.fiap.oficina_mecanica.cliente.infrastructure.entity.ClienteEntity;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class ClienteRepositoryAdapter implements ClienteRepository {

    private final ClienteJpaRepository jpaRepository;

    public ClienteRepositoryAdapter(ClienteJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public boolean existePorId(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public Optional<String> buscarDocumentoPorId(UUID id) {
        return jpaRepository.findById(id).map(ClienteEntity::getDocumento);
    }
}