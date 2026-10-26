package br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.usecase;

import br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.dto.ClienteResumoOutput;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public interface ConsultorClientes {
    Map<UUID, ClienteResumoOutput> resumirPorIds(Collection<UUID> clienteIds);
}
