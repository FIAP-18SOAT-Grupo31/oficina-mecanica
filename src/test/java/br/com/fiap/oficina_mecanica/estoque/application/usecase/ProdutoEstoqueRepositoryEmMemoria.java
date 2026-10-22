package br.com.fiap.oficina_mecanica.estoque.application.usecase;

import br.com.fiap.oficina_mecanica.estoque.domain.ProdutoEstoque;
import br.com.fiap.oficina_mecanica.estoque.domain.ProdutoEstoqueRepository;

import java.util.HashMap;
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

    public boolean isEmpty() {
        return produtos.isEmpty();
    }
}