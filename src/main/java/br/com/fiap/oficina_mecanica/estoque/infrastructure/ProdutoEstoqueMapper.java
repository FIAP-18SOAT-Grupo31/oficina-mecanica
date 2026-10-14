package br.com.fiap.oficina_mecanica.estoque.infrastructure;

import br.com.fiap.oficina_mecanica.estoque.domain.ProdutoEstoque;
import br.com.fiap.oficina_mecanica.estoque.infrastructure.entity.ProdutoEstoqueEntity;
import org.springframework.stereotype.Component;

@Component
public class ProdutoEstoqueMapper {

    public ProdutoEstoqueEntity toEntity(ProdutoEstoque dominio) {
        return ProdutoEstoqueEntity.builder()
                .id(dominio.getId())
                .nomePeca(dominio.getNomePeca())
                .quantidadeDisponivel(dominio.getQuantidadeDisponivel())
                .quantidadeReservada(dominio.getQuantidadeReservada())
                .valorUnitario(dominio.getValorUnitario())
                .build();
    }

    public ProdutoEstoque toDomain(ProdutoEstoqueEntity entity) {
        return ProdutoEstoque.builder()
                .id(entity.getId())
                .nomePeca(entity.getNomePeca())
                .quantidadeDisponivel(entity.getQuantidadeDisponivel())
                .quantidadeReservada(entity.getQuantidadeReservada())
                .valorUnitario(entity.getValorUnitario())
                .build();
    }
}