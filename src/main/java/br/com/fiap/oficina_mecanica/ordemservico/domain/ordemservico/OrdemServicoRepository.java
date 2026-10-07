package br.com.fiap.oficina_mecanica.ordemservico.domain.ordemservico;

import java.util.Optional;
import java.util.UUID;

public interface OrdemServicoRepository {
    OrdemServico salvar(OrdemServico ordemServico);
    Optional<OrdemServico> buscarPorId(UUID id);
}
