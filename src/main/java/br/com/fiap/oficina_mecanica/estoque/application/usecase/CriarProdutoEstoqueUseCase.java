package br.com.fiap.oficina_mecanica.estoque.application.usecase;

import br.com.fiap.oficina_mecanica.compartilhado.domain.exception.RegraNegocioException;
import br.com.fiap.oficina_mecanica.estoque.application.dto.CriarProdutoEstoqueInput;
import br.com.fiap.oficina_mecanica.estoque.application.dto.EstoqueOutput;
import br.com.fiap.oficina_mecanica.estoque.domain.ProdutoEstoque;
import br.com.fiap.oficina_mecanica.estoque.domain.ProdutoEstoqueRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static br.com.fiap.oficina_mecanica.estoque.application.dto.EstoqueOutput.deDominio;

@Service
public class CriarProdutoEstoqueUseCase {

    private final ProdutoEstoqueRepository repository;

    public CriarProdutoEstoqueUseCase(ProdutoEstoqueRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public EstoqueOutput execute(CriarProdutoEstoqueInput input) {
        repository.buscarPorNomePeca(input.nomePeca())
                .ifPresent(p -> {
                    throw new RegraNegocioException("Já existe uma peça cadastrada no estoque com o nome: " + input.nomePeca());
                });

        ProdutoEstoque novoProduto = ProdutoEstoque.builder()
                .id(UUID.randomUUID())
                .nomePeca(input.nomePeca())
                .quantidadeDisponivel(input.quantidadeDisponivel())
                .quantidadeReservada(0)
                .valorUnitario(input.valorUnitario())
                .build();

        ProdutoEstoque produtoSalvo = repository.salvar(novoProduto);

        return deDominio(produtoSalvo);
    }
}