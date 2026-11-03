package br.com.fiap.oficina_mecanica.servico.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "servicos")
public class ServicoEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 100)
    private String nome;

    @Column(length = 255)
    private String descricao;

    @Column(name = "valor_mao_de_obra", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorMaoDeObra;

    protected ServicoEntity() {}

    public ServicoEntity(UUID id, String nome, String descricao, BigDecimal valorMaoDeObra) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.valorMaoDeObra = valorMaoDeObra;
    }

    public UUID getId() { return id; }
    public String getNome() { return nome; }
    public String getDescricao() { return descricao; }
    public BigDecimal getValorMaoDeObra() { return valorMaoDeObra; }
}
