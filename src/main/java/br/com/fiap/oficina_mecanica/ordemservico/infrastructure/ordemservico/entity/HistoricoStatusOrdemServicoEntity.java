package br.com.fiap.oficina_mecanica.ordemservico.infrastructure.ordemservico.entity;

import br.com.fiap.oficina_mecanica.ordemservico.domain.ordemservico.StatusOrdemServico;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "historico_status_ordem_servico")
public class HistoricoStatusOrdemServicoEntity {

    @Id
    private UUID id;

    @Column(name = "ordem_servico_id", nullable = false, updatable = false)
    private UUID ordemServicoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_anterior", length = 30, updatable = false)
    private StatusOrdemServico statusAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_novo", nullable = false, length = 30, updatable = false)
    private StatusOrdemServico statusNovo;

    @Column(name = "data_transicao", nullable = false, updatable = false)
    private LocalDateTime dataTransicao;

    protected HistoricoStatusOrdemServicoEntity() {}

    public HistoricoStatusOrdemServicoEntity(UUID id, UUID ordemServicoId, StatusOrdemServico statusAnterior,
                                             StatusOrdemServico statusNovo, LocalDateTime dataTransicao) {
        this.id = id;
        this.ordemServicoId = ordemServicoId;
        this.statusAnterior = statusAnterior;
        this.statusNovo = statusNovo;
        this.dataTransicao = dataTransicao;
    }

    public UUID getId() { return id; }
    public UUID getOrdemServicoId() { return ordemServicoId; }
    public StatusOrdemServico getStatusAnterior() { return statusAnterior; }
    public StatusOrdemServico getStatusNovo() { return statusNovo; }
    public LocalDateTime getDataTransicao() { return dataTransicao; }
}
