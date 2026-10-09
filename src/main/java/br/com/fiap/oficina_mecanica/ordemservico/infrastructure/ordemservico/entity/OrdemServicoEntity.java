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
@Table(name = "ordens_servico")
public class OrdemServicoEntity {

    @Id
    private UUID id;

    @Column(name = "cliente_id", nullable = false)
    private UUID clienteId;

    @Column(name = "veiculo_id", nullable = false)
    private UUID veiculoId;

    @Column(name = "relato_problema", nullable = false, length = 500)
    private String relatoProblema;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusOrdemServico status;

    @Column(name = "data_abertura", nullable = false, updatable = false)
    private LocalDateTime dataAbertura;

    @Column(name = "data_conclusao")
    private LocalDateTime dataConclusao;

    @Column(name = "codigo_acesso", nullable = false, length = 6, updatable = false)
    private String codigoAcesso;

    protected OrdemServicoEntity() {}

    public OrdemServicoEntity(UUID id, UUID clienteId, UUID veiculoId, String relatoProblema,
                              StatusOrdemServico status, LocalDateTime dataAbertura, LocalDateTime dataConclusao,
                              String codigoAcesso) {
        this.id = id;
        this.clienteId = clienteId;
        this.veiculoId = veiculoId;
        this.relatoProblema = relatoProblema;
        this.status = status;
        this.dataAbertura = dataAbertura;
        this.dataConclusao = dataConclusao;
        this.codigoAcesso = codigoAcesso;
    }

    public UUID getId() { return id; }
    public UUID getClienteId() { return clienteId; }
    public UUID getVeiculoId() { return veiculoId; }
    public String getRelatoProblema() { return relatoProblema; }
    public StatusOrdemServico getStatus() { return status; }
    public LocalDateTime getDataAbertura() { return dataAbertura; }
    public LocalDateTime getDataConclusao() { return dataConclusao; }
    public String getCodigoAcesso() { return codigoAcesso; }
}