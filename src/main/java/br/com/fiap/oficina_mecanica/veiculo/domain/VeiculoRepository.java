package br.com.fiap.oficina_mecanica.veiculo.domain;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VeiculoRepository {
    boolean existePorId(UUID id);
    List<VeiculoResumo> buscarResumosPorIds(Collection<UUID> ids);
    Optional<VeiculoDetalhe> buscarDetalhePorId(UUID id);
}
