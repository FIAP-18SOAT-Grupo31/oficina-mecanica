package br.com.fiap.oficina_mecanica.ordemservico.domain.ordemservico;

import java.util.Optional;
import java.util.UUID;

public interface OrdemServicoRepository {

    Optional<OrdemServico> findById(UUID id);
    OrdemServico save(OrdemServico ordemServico);
}
