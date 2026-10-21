package br.com.fiap.oficina_mecanica.cliente.application.usecase;

import br.com.fiap.oficina_mecanica.cliente.domain.Cliente;
import br.com.fiap.oficina_mecanica.cliente.domain.ClienteRepository;
import br.com.fiap.oficina_mecanica.compartilhado.domain.Pagina;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

class ClienteRepositoryEmMemoria implements ClienteRepository {

    final Map<UUID, Cliente> clientes = new LinkedHashMap<>();

    @Override
    public Cliente salvar(Cliente cliente) {
        clientes.put(cliente.getId(), cliente);
        return cliente;
    }

    @Override
    public Optional<Cliente> buscarPorId(UUID id) {
        return Optional.ofNullable(clientes.get(id));
    }

    @Override
    public Pagina<Cliente> listar(String nome, String documento, int pagina, int tamanho) {
        List<Cliente> filtrados = clientes.values().stream()
                .filter(cliente -> nome == null || cliente.getNome().toLowerCase().contains(nome.toLowerCase()))
                .filter(cliente -> documento == null || cliente.getDocumento().equals(documento))
                .sorted(Comparator.comparing(Cliente::getNome))
                .toList();

        int inicio = Math.min(pagina * tamanho, filtrados.size());
        int fim = Math.min(inicio + tamanho, filtrados.size());
        return new Pagina<>(filtrados.subList(inicio, fim), pagina, tamanho, filtrados.size());
    }

    @Override
    public boolean existePorId(UUID id) {
        return clientes.containsKey(id);
    }

    @Override
    public boolean existePorDocumento(String documento) {
        return clientes.values().stream().anyMatch(cliente -> cliente.getDocumento().equals(documento));
    }

    @Override
    public boolean existeOutroComDocumento(String documento, UUID idIgnorado) {
        return clientes.values().stream()
                .anyMatch(cliente -> !cliente.getId().equals(idIgnorado) && cliente.getDocumento().equals(documento));
    }

    @Override
    public void excluirPorId(UUID id) {
        clientes.remove(id);
    }

    @Override
    public Optional<String> buscarDocumentoPorId(UUID id) {
        return buscarPorId(id).map(Cliente::getDocumento);
    }
}
