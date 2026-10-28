package br.com.fiap.oficina_mecanica.veiculo.application.usecase;

import br.com.fiap.oficina_mecanica.compartilhado.domain.Pagina;
import br.com.fiap.oficina_mecanica.veiculo.domain.Veiculo;
import br.com.fiap.oficina_mecanica.veiculo.domain.VeiculoRepository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

class VeiculoRepositoryEmMemoria implements VeiculoRepository {

    final Map<UUID, Veiculo> veiculos = new LinkedHashMap<>();

    @Override
    public Veiculo salvar(Veiculo veiculo) {
        veiculos.put(veiculo.getId(), veiculo);
        return veiculo;
    }

    @Override
    public Optional<Veiculo> buscarPorId(UUID id) {
        return Optional.ofNullable(veiculos.get(id));
    }

    @Override
    public Pagina<Veiculo> listar(UUID clienteId, String placa, String modelo, int pagina, int tamanho) {
        List<Veiculo> todos = List.copyOf(veiculos.values());
        return new Pagina<>(todos, pagina, tamanho, todos.size());
    }

    @Override
    public boolean existePorId(UUID id) {
        return veiculos.containsKey(id);
    }

    @Override
    public boolean existePorClienteId(UUID clienteId) {
        return veiculos.values().stream().anyMatch(veiculo -> veiculo.getClienteId().equals(clienteId));
    }

    @Override
    public boolean existePorPlaca(String placa) {
        return veiculos.values().stream().anyMatch(veiculo -> veiculo.getPlaca().equals(placa));
    }

    @Override
    public boolean existeOutroComPlaca(String placa, UUID idIgnorado) {
        return veiculos.values().stream()
                .anyMatch(veiculo -> !veiculo.getId().equals(idIgnorado) && veiculo.getPlaca().equals(placa));
    }

    @Override
    public void excluirPorId(UUID id) {
        veiculos.remove(id);
    }
}
