package br.com.fiap.oficina_mecanica.ordemservico.domain;

import br.com.fiap.oficina_mecanica.ordemservico.domain.orcamento.Orcamento;

import java.time.LocalDateTime;
import java.util.UUID;

public class OrdemServico {

    private UUID id;

    private UUID clienteId;
    private UUID veiculoId;

    private String relatoProblema;
    private StatusOS status;
    private Orcamento orcamento;
    private LocalDateTime dataAbertura;
    private LocalDateTime dataConclusao;

    public OrdemServico(UUID clienteId, UUID veiculoId, String relatoProblema) {
        this.id = UUID.randomUUID();
        this.clienteId = clienteId;
        this.veiculoId = veiculoId;
        this.relatoProblema = relatoProblema;
        this.status = StatusOS.RECEBIDA;
        this.dataAbertura = LocalDateTime.now();
    }

    public void iniciarDiagnostico() {
        if (this.status != StatusOS.RECEBIDA) {
            throw new IllegalStateException("O diagnóstico só pode ser iniciado em uma OS recém-recebida.");
        }
        this.status = StatusOS.EM_DIAGNOSTICO;
    }

    public void anexarOrcamento(Orcamento orcamento) {
        if (this.status != StatusOS.EM_DIAGNOSTICO) {
            throw new IllegalStateException("O orçamento só pode ser anexado após a fase de diagnóstico.");
        }
        this.orcamento = orcamento;
        this.status = StatusOS.AGUARDANDO_APROVACAO;
    }

    public void aprovarOrcamento() {
        if (this.status != StatusOS.AGUARDANDO_APROVACAO) {
            throw new IllegalStateException("Apenas ordens aguardando aprovação podem ser aprovadas.");
        }
        this.status = StatusOS.APROVADA;
    }

    public void recusarOrcamento() {
        if (this.status != StatusOS.AGUARDANDO_APROVACAO) {
            throw new IllegalStateException("Apenas ordens aguardando aprovação podem ser recusadas.");
        }
        this.status = StatusOS.RECUSADA;
    }
}