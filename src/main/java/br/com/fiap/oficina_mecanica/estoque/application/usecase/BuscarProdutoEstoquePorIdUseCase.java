package br.com.fiap.oficina_mecanica.estoque.application.usecase;

import br.com.fiap.oficina_mecanica.compartilhado.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.oficina_mecanica.estoque.application.dto.EstoqueOutput;
import br.com.fiap.oficina_mecanica.estoque.domain.ProdutoEstoque;
import br.com.fiap.oficina_mecanica.estoque.domain.ProdutoEstoqueRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static br.com.fiap.oficina_mecanica.estoque.application.dto.EstoqueOutput.deDominio;

@Service
public class BuscarProdutoEstoquePorIdUseCase {

    private final ProdutoEstoqueRepository repository;

    public BuscarProdutoEstoquePorIdUseCase(ProdutoEstoqueRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public EstoqueOutput execute(UUID id) {
        ProdutoEstoque produto = repository.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Peça não encontrada no estoque com o ID: " + id));

        return deDominio(produto);
    }
}