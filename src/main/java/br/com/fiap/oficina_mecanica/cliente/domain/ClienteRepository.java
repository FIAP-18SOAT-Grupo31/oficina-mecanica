package br.com.fiap.oficina_mecanica.cliente.domain;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClienteRepository {
    boolean existePorId(UUID id);
    Optional<String> buscarDocumentoPorId(UUID id);
    List<ClienteResumo> buscarResumosPorIds(Collection<UUID> ids);
    Optional<ClienteDetalhe> buscarDetalhePorId(UUID id);
}