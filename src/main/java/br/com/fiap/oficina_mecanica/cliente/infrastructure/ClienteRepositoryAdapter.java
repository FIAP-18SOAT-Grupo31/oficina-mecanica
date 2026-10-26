package br.com.fiap.oficina_mecanica.cliente.infrastructure;

import br.com.fiap.oficina_mecanica.cliente.domain.ClienteDetalhe;
import br.com.fiap.oficina_mecanica.cliente.domain.ClienteRepository;
import br.com.fiap.oficina_mecanica.cliente.domain.ClienteResumo;
import br.com.fiap.oficina_mecanica.cliente.infrastructure.entity.ClienteEntity;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
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

    @Override
    public List<ClienteResumo> buscarResumosPorIds(Collection<UUID> ids) {
        return jpaRepository.findAllById(ids).stream()
                .map(cliente -> new ClienteResumo(cliente.getId(), cliente.getNome()))
                .toList();
    }

    @Override
    public Optional<ClienteDetalhe> buscarDetalhePorId(UUID id) {
        return jpaRepository.findById(id)
                .map(c -> new ClienteDetalhe(c.getId(), c.getNome(), c.getDocumento(), c.getEmail(), c.getTelefone()));
    }
}