package br.com.fiap.oficina_mecanica.ordemservico.infrastructure.orcamento;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "orcamento_itens_servico")
public class ItemServicoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "catalogo_servico_id", nullable = false)
    private UUID catalogoServicoId;

    @Column(nullable = false)
    private String descricao;

    @Column(name = "valor_mao_de_obra", nullable = false)
    private BigDecimal valorMaoDeObra;

    protected ItemServicoEntity() {}

    public ItemServicoEntity(UUID catalogoServicoId, String descricao, BigDecimal valorMaoDeObra) {
        this.catalogoServicoId = catalogoServicoId;
        this.descricao = descricao;
        this.valorMaoDeObra = valorMaoDeObra;
    }

    public UUID getId() { return id; }
    public UUID getCatalogoServicoId() { return catalogoServicoId; }
    public String getDescricao() { return descricao; }
    public BigDecimal getValorMaoDeObra() { return valorMaoDeObra; }
}