package br.com.fiap.oficina_mecanica.estoque.application.usecase;

import br.com.fiap.oficina_mecanica.compartilhado.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.oficina_mecanica.compartilhado.domain.exception.RegraNegocioException;
import br.com.fiap.oficina_mecanica.estoque.application.dto.AtualizarProdutoEstoqueInput;
import br.com.fiap.oficina_mecanica.estoque.application.dto.EstoqueOutput;
import br.com.fiap.oficina_mecanica.estoque.domain.ProdutoEstoque;
import br.com.fiap.oficina_mecanica.estoque.domain.ProdutoEstoqueRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static br.com.fiap.oficina_mecanica.estoque.application.dto.EstoqueOutput.deDominio;

@Service
public class AtualizarProdutoEstoqueUseCase {

    private final ProdutoEstoqueRepository repository;

    public AtualizarProdutoEstoqueUseCase(ProdutoEstoqueRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public EstoqueOutput execute(UUID id, AtualizarProdutoEstoqueInput input) {
        ProdutoEstoque produto = repository.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Peça não encontrada no estoque com o ID: " + id));

        repository.buscarPorNomePeca(input.nomePeca())
                .filter(outro -> !outro.getId().equals(id))
                .ifPresent(outro -> {
                    throw new RegraNegocioException("Já existe uma peça cadastrada no estoque com o nome: " + input.nomePeca());
                });

        produto.atualizar(input.nomePeca(), input.valorUnitario());

        ProdutoEstoque produtoSalvo = repository.salvar(produto);
        return deDominio(produtoSalvo);
    }
}
