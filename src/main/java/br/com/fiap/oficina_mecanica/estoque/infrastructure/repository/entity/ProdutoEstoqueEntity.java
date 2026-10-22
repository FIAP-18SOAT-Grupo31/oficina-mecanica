package br.com.fiap.oficina_mecanica.estoque.infrastructure.repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "produtos_estoque")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProdutoEstoqueEntity {

    @Id
    private UUID id;

    @Column(name = "nome_peca", nullable = false)
    private String nomePeca;

    @Column(name = "quantidade_disponivel", nullable = false)
    private int quantidadeDisponivel;

    @Column(name = "quantidade_reservada", nullable = false)
    private int quantidadeReservada;

    @Column(name = "valor_unitario", nullable = false)
    private BigDecimal valorUnitario;
}