package br.com.fiap.oficina_mecanica.estoque.application.usecase;

import br.com.fiap.oficina_mecanica.compartilhado.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.oficina_mecanica.estoque.application.dto.AjustarQuantidadeInput;
import br.com.fiap.oficina_mecanica.estoque.application.dto.EstoqueOutput;
import br.com.fiap.oficina_mecanica.estoque.domain.ProdutoEstoque;
import br.com.fiap.oficina_mecanica.estoque.domain.ProdutoEstoqueRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static br.com.fiap.oficina_mecanica.estoque.application.dto.EstoqueOutput.deDominio;

@Service
public class AdicionarEstoqueUseCase {

    private final ProdutoEstoqueRepository repository;

    public AdicionarEstoqueUseCase(ProdutoEstoqueRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public EstoqueOutput execute(UUID id, AjustarQuantidadeInput input) {
        ProdutoEstoque produto = repository.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Peça não encontrada no estoque com o ID: " + id));

        produto.adicionarEstoque(input.quantidade());

        ProdutoEstoque produtoSalvo = repository.salvar(produto);
        return deDominio(produtoSalvo);
    }
}