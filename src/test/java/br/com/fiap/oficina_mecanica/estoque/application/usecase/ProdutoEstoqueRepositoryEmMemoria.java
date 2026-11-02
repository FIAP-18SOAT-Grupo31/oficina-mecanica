package br.com.fiap.oficina_mecanica.estoque.application.usecase;

import br.com.fiap.oficina_mecanica.compartilhado.domain.Pagina;
import br.com.fiap.oficina_mecanica.estoque.domain.ProdutoEstoque;
import br.com.fiap.oficina_mecanica.estoque.domain.ProdutoEstoqueRepository;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

class ProdutoEstoqueRepositoryEmMemoria implements ProdutoEstoqueRepository {

    private final Map<UUID, ProdutoEstoque> produtos = new HashMap<>();

    @Override
    public ProdutoEstoque salvar(ProdutoEstoque produto) {
        produtos.put(produto.getId(), produto);
        return produto;
    }

    @Override
    public Optional<ProdutoEstoque> buscarPorId(UUID id) {
        return Optional.ofNullable(produtos.get(id));
    }

    @Override
    public Optional<ProdutoEstoque> buscarPorNomePeca(String nomePeca) {
        return produtos.values().stream()
                .filter(p -> p.getNomePeca().equalsIgnoreCase(nomePeca))
                .findFirst();
    }

    @Override
    public Pagina<ProdutoEstoque> listar(String nomePeca, int pagina, int tamanho) {
        List<ProdutoEstoque> filtrados = produtos.values().stream()
                .filter(p -> nomePeca == null || p.getNomePeca().toLowerCase().contains(nomePeca.toLowerCase()))
                .sorted(Comparator.comparing(ProdutoEstoque::getNomePeca))
                .toList();
        List<ProdutoEstoque> conteudo = filtrados.stream()
                .skip((long) pagina * tamanho)
                .limit(tamanho)
                .toList();
        return new Pagina<>(conteudo, pagina, tamanho, filtrados.size());
    }

    @Override
    public void excluirPorId(UUID id) {
        produtos.remove(id);
    }

    public boolean isEmpty() {
        return produtos.isEmpty();
    }
}