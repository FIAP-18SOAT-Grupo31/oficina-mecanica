package br.com.fiap.oficina_mecanica.estoque.application.dto;

import br.com.fiap.oficina_mecanica.estoque.domain.ProdutoEstoque;

import java.math.BigDecimal;
import java.util.UUID;

public record EstoqueOutput(
        UUID id,
        String nomePeca,
        BigDecimal valorUnitario,
        int quantidadeDisponivel,
        int quantidadeReservada
) {
    public static EstoqueOutput deDominio(ProdutoEstoque produto) {
        return new EstoqueOutput(
                produto.getId(),
                produto.getNomePeca(),
                produto.getValorUnitario(),
                produto.getQuantidadeDisponivel(),
                produto.getQuantidadeReservada()
                );
    }
}