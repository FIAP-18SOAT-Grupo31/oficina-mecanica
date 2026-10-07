package br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.usecase;

import br.com.fiap.oficina_mecanica.ordemservico.domain.orcamento.Orcamento;
import br.com.fiap.oficina_mecanica.ordemservico.domain.orcamento.OrcamentoRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

class OrcamentoRepositoryEmMemoria implements OrcamentoRepository {

    private final Map<UUID, Orcamento> orcamentos = new HashMap<>();

    @Override
    public Orcamento salvar(Orcamento orcamento) {
        orcamentos.put(orcamento.getId(), orcamento);
        return orcamento;
    }

    @Override
    public Optional<Orcamento> buscarPorId(UUID id) {
        return Optional.ofNullable(orcamentos.get(id));
    }

    @Override
    public Optional<Orcamento> buscarPorOrdemServicoId(UUID ordemServicoId) {
        return orcamentos.values().stream()
                .filter(orcamento -> orcamento.getOrdemServicoId().equals(ordemServicoId))
                .findFirst();
    }
}
