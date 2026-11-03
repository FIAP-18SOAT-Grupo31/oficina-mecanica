package br.com.fiap.oficina_mecanica.ordemservico.domain.orcamento;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrcamentoRepository {
    Orcamento salvar(Orcamento orcamento);
    Optional<Orcamento> buscarPorId(UUID id);
    List<Orcamento> buscarPorOrdemServicoId(UUID ordemServicoId);
}