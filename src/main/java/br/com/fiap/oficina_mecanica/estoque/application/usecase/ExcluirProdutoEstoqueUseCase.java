package br.com.fiap.oficina_mecanica.estoque.application.usecase;

import br.com.fiap.oficina_mecanica.compartilhado.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.oficina_mecanica.estoque.domain.ProdutoEstoque;
import br.com.fiap.oficina_mecanica.estoque.domain.ProdutoEstoqueRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ExcluirProdutoEstoqueUseCase {

    private final ProdutoEstoqueRepository repository;

    public ExcluirProdutoEstoqueUseCase(ProdutoEstoqueRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void execute(UUID id) {
        ProdutoEstoque produto = repository.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Peça não encontrada no estoque com o ID: " + id));

        // o orçamento guarda nome e valor da peça, então só bloqueia enquanto houver reserva
        produto.validarExclusao();

        repository.excluirPorId(id);
    }
}
