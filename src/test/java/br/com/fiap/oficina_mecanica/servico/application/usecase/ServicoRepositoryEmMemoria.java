package br.com.fiap.oficina_mecanica.servico.application.usecase;

import br.com.fiap.oficina_mecanica.compartilhado.domain.Pagina;
import br.com.fiap.oficina_mecanica.servico.domain.Servico;
import br.com.fiap.oficina_mecanica.servico.domain.ServicoRepository;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class ServicoRepositoryEmMemoria implements ServicoRepository {

    final Map<UUID, Servico> servicos = new LinkedHashMap<>();

    @Override
    public Servico salvar(Servico servico) {
        servicos.put(servico.getId(), servico);
        return servico;
    }

    @Override
    public Optional<Servico> buscarPorId(UUID id) {
        return Optional.ofNullable(servicos.get(id));
    }

    @Override
    public Pagina<Servico> listar(String nome, int pagina, int tamanho) {
        List<Servico> filtrados = servicos.values().stream()
                .filter(servico -> nome == null || servico.getNome().toLowerCase().contains(nome.toLowerCase()))
                .sorted(Comparator.comparing(Servico::getNome))
                .toList();

        int inicio = Math.min(pagina * tamanho, filtrados.size());
        int fim = Math.min(inicio + tamanho, filtrados.size());
        return new Pagina<>(filtrados.subList(inicio, fim), pagina, tamanho, filtrados.size());
    }

    @Override
    public boolean existePorId(UUID id) {
        return servicos.containsKey(id);
    }

    @Override
    public boolean existePorNome(String nome) {
        return servicos.values().stream().anyMatch(servico -> servico.getNome().equalsIgnoreCase(nome));
    }

    @Override
    public boolean existeOutroComNome(String nome, UUID idIgnorado) {
        return servicos.values().stream()
                .anyMatch(servico -> !servico.getId().equals(idIgnorado) && servico.getNome().equalsIgnoreCase(nome));
    }

    @Override
    public void excluirPorId(UUID id) {
        servicos.remove(id);
    }
}
