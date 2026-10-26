package br.com.fiap.oficina_mecanica.ordemservico.infrastructure.ordemservico;

import br.com.fiap.oficina_mecanica.cliente.domain.ClienteRepository;
import br.com.fiap.oficina_mecanica.cliente.domain.ClienteResumo;
import br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.dto.ClienteResumoOutput;
import br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.usecase.ConsultorClientes;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ConsultorClientesAdapter implements ConsultorClientes {

    private final ClienteRepository clienteRepository;

    public ConsultorClientesAdapter(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Override
    public Map<UUID, ClienteResumoOutput> resumirPorIds(Collection<UUID> clienteIds) {
        return clienteRepository.buscarResumosPorIds(clienteIds).stream()
                .collect(Collectors.toMap(ClienteResumo::id, paraOutput(), (a, b) -> a));
    }

    private static Function<ClienteResumo, ClienteResumoOutput> paraOutput() {
        return cliente -> new ClienteResumoOutput(cliente.id(), cliente.nome());
    }
}
