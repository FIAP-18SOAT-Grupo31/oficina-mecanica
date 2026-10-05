package br.com.fiap.oficina_mecanica.ordemservico.infrastructure.orcamento;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "orcamento_itens_peca")
public class ItemPecaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "produto_estoque_id", nullable = false)
    private UUID produtoEstoqueId;

    @Column(name = "nome_peca", nullable = false)
    private String nomePeca;

    @Column(nullable = false)
    private Integer quantidade;

    @Column(name = "valor_unitario", nullable = false)
    private BigDecimal valorUnitario;

    protected ItemPecaEntity() {}

    public ItemPecaEntity(UUID produtoEstoqueId, String nomePeca, Integer quantidade, BigDecimal valorUnitario) {
        this.produtoEstoqueId = produtoEstoqueId;
        this.nomePeca = nomePeca;
        this.quantidade = quantidade;
        this.valorUnitario = valorUnitario;
    }

    public UUID getId() { return id; }
    public UUID getProdutoEstoqueId() { return produtoEstoqueId; }
    public String getNomePeca() { return nomePeca; }
    public Integer getQuantidade() { return quantidade; }
    public BigDecimal getValorUnitario() { return valorUnitario; }
}