package br.com.fiap.oficina_mecanica.ordemservico.infrastructure.orcamento.entity;

import br.com.fiap.oficina_mecanica.ordemservico.domain.orcamento.StatusOrcamento;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orcamentos")
public class OrcamentoEntity {

    @Id
    private UUID id;

    @Column(name = "ordem_servico_id", nullable = false)
    private UUID ordemServicoId;

    @Column(name = "valor_total", nullable = false)
    private BigDecimal valorTotal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusOrcamento status;

    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "data_validade", nullable = false)
    private LocalDateTime dataValidade;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "orcamento_id")
    private List<ItemServicoEntity> servicos = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "orcamento_id")
    private List<ItemPecaEntity> pecas = new ArrayList<>();

    protected OrcamentoEntity() {}

    public OrcamentoEntity(UUID id, UUID ordemServicoId, BigDecimal valorTotal, StatusOrcamento status, LocalDateTime dataCriacao, LocalDateTime dataValidade) {
        this.id = id;
        this.ordemServicoId = ordemServicoId;
        this.valorTotal = valorTotal;
        this.status = status;
        this.dataCriacao = dataCriacao;
        this.dataValidade = dataValidade;
    }

    public UUID getId() { return id; }
    public UUID getOrdemServicoId() { return ordemServicoId; }
    public BigDecimal getValorTotal() { return valorTotal; }
    public StatusOrcamento getStatus() { return status; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public LocalDateTime getDataValidade() { return dataValidade; }
    public List<ItemServicoEntity> getServicos() { return servicos; }
    public List<ItemPecaEntity> getPecas() { return pecas; }

    public void setOrdemServicoId(UUID ordemServicoId) { this.ordemServicoId = ordemServicoId; }
    public void setValorTotal(BigDecimal valorTotal) { this.valorTotal = valorTotal; }
    public void setStatus(StatusOrcamento status) { this.status = status; }
    public void setDataValidade(LocalDateTime dataValidade) { this.dataValidade = dataValidade; }
    public void setServicos(List<ItemServicoEntity> servicos) { this.servicos = servicos; }
    public void setPecas(List<ItemPecaEntity> pecas) { this.pecas = pecas; }
}