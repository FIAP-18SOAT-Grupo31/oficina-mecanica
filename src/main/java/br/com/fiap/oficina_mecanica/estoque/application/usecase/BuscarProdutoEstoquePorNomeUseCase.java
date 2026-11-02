package br.com.fiap.oficina_mecanica.estoque.application.usecase;

import br.com.fiap.oficina_mecanica.compartilhado.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.oficina_mecanica.estoque.application.dto.EstoqueOutput;
import br.com.fiap.oficina_mecanica.estoque.domain.ProdutoEstoque;
import br.com.fiap.oficina_mecanica.estoque.domain.ProdutoEstoqueRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static br.com.fiap.oficina_mecanica.estoque.application.dto.EstoqueOutput.deDominio;

@Service
public class BuscarProdutoEstoquePorNomeUseCase {

    private final ProdutoEstoqueRepository repository;

    public BuscarProdutoEstoquePorNomeUseCase(ProdutoEstoqueRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public EstoqueOutput execute(String nomePeca) {
        if (nomePeca == null || nomePeca.isBlank()) {
            throw new IllegalArgumentException("O nome da peça não pode ser nulo ou vazio.");
        }

        ProdutoEstoque produto = repository.buscarPorNomePeca(nomePeca.trim().toUpperCase())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Peça não encontrada no estoque com o nome: " + nomePeca));

        return deDominio(produto);
    }
}