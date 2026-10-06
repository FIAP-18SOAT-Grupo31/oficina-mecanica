package br.com.fiap.oficina_mecanica.ordemservico.domain.orcamento;

import java.util.Optional;
import java.util.UUID;

public interface OrcamentoRepository {
    Orcamento salvar(Orcamento orcamento);
    Optional<Orcamento> buscarPorId(UUID id);
    Optional<Orcamento> buscarPorOrdemServicoId(UUID ordemServicoId);
}