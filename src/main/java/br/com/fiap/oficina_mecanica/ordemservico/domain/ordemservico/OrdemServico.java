package br.com.fiap.oficina_mecanica.ordemservico.domain.ordemservico;

import br.com.fiap.oficina_mecanica.ordemservico.domain.orcamento.Orcamento;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OrdemServico {

    private UUID id;

    private UUID clienteId;
    private UUID veiculoId;

    private String relatoProblema;
    private StatusOrdemServico status;
    private List<Orcamento> orcamentos;
    private LocalDateTime dataAbertura;
    private LocalDateTime dataConclusao;

    public OrdemServico(UUID id, UUID clienteId, UUID veiculoId, String relatoProblema) {
        if (id == null) {
            throw new IllegalArgumentException("O ID da ordem de serviço não pode ser nulo.");
        }

        this.id = id;
        this.clienteId = clienteId;
        this.veiculoId = veiculoId;
        this.status = StatusOrdemServico.RECEBIDA;
        this.dataAbertura = LocalDateTime.now();
        
        validarRelatoProblema(relatoProblema);
        this.relatoProblema = relatoProblema;
    }

    public void iniciarDiagnostico() {
        if (this.status != StatusOrdemServico.RECEBIDA) {
            throw new IllegalStateException("O diagnóstico só pode ser iniciado em uma OS recém-recebida.");
        }
        this.status = StatusOrdemServico.EM_DIAGNOSTICO;
    }

    public void anexarOrcamento(Orcamento orcamento) {
        if (this.status != StatusOrdemServico.EM_DIAGNOSTICO) {
            throw new IllegalStateException("O orçamento só pode ser anexado após a fase de diagnóstico.");
        }

        // TODO: adicionar validacao para verificar se orçamento existe, está aprovado ou aguardando aprovação, etc

        this.orcamentos.add(orcamento);
        this.status = StatusOrdemServico.AGUARDANDO_APROVACAO;
    }

    public void aprovarOrcamento() {
        if (this.status != StatusOrdemServico.AGUARDANDO_APROVACAO) {
            throw new IllegalStateException("Apenas ordens aguardando aprovação podem ser aprovadas.");
        }
        this.status = StatusOrdemServico.APROVADA;
    }

    public void recusarOrcamento() {
        if (this.status != StatusOrdemServico.AGUARDANDO_APROVACAO) {
            throw new IllegalStateException("Apenas ordens aguardando aprovação podem ser recusadas.");
        }
        this.status = StatusOrdemServico.RECUSADA;
    }
    
    private void validarRelatoProblema(String relato) {
        if (relato == null || relato.isBlank()) {
            throw new IllegalArgumentException("O relato do problema não pode ser nulo ou vazio.");
        }

        if (relato.length() < 10 || relato.length() > 500) {
            throw new IllegalArgumentException("O relato do problema deve ter entre 10 e 500 caracteres.");
        }
    }
}