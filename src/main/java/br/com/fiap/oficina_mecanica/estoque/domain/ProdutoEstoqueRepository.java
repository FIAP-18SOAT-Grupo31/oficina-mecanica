package br.com.fiap.oficina_mecanica.estoque.domain;

import br.com.fiap.oficina_mecanica.compartilhado.domain.Pagina;

import java.util.Optional;
import java.util.UUID;

public interface ProdutoEstoqueRepository {
    ProdutoEstoque salvar(ProdutoEstoque produto);
    Optional<ProdutoEstoque> buscarPorId(UUID id);
    Optional<ProdutoEstoque> buscarPorNomePeca(String nomePeca);
    Pagina<ProdutoEstoque> listar(String nomePeca, int pagina, int tamanho);
    void excluirPorId(UUID id);
}