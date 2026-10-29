package br.com.fiap.oficina_mecanica.estoque.domain;

import java.util.Optional;
import java.util.UUID;

public interface ProdutoEstoqueRepository {
    ProdutoEstoque salvar(ProdutoEstoque produto);
    Optional<ProdutoEstoque> buscarPorId(UUID id);
    Optional<ProdutoEstoque> buscarPorNomePeca(String nomePeca);
}