package br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.usecase;

import br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.dto.VeiculoDetalheOutput;
import br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.dto.VeiculoResumoOutput;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface ConsultorVeiculos {
    Map<UUID, VeiculoResumoOutput> resumirPorIds(Collection<UUID> veiculoIds);
    Optional<VeiculoDetalheOutput> detalharPorId(UUID veiculoId);
}
