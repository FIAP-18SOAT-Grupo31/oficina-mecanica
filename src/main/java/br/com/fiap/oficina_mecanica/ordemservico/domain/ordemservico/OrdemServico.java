package br.com.fiap.oficina_mecanica.ordemservico.domain.ordemservico;

import br.com.fiap.oficina_mecanica.compartilhado.domain.Horario;
import br.com.fiap.oficina_mecanica.compartilhado.domain.exception.RegraNegocioException;
import br.com.fiap.oficina_mecanica.ordemservico.domain.orcamento.Orcamento;
import com.github.f4b6a3.uuid.UuidCreator;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
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
    private String codigoAcesso;
    // Só as transições ainda não gravadas; o histórico completo é lido sob demanda, não junto da OS.
    @Builder.Default
    private List<TransicaoStatus> novasTransicoes = new ArrayList<>();

    public OrdemServico(UUID id, UUID clienteId, UUID veiculoId, String relatoProblema) {
        if (id == null) {
            throw new IllegalArgumentException("O ID da ordem de serviço não pode ser nulo.");
        }

        this.id = id;
        this.clienteId = clienteId;
        this.veiculoId = veiculoId;
        this.novasTransicoes = new ArrayList<>();
        this.orcamentos = new ArrayList<>();
        this.dataAbertura = Horario.agora();
        registrarTransicao(StatusOrdemServico.RECEBIDA, this.dataAbertura);

        this.codigoAcesso = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        
        validarRelatoProblema(relatoProblema);
        this.relatoProblema = relatoProblema;
    }

    public void iniciarDiagnostico() {
        if (this.status != StatusOrdemServico.RECEBIDA) {
            throw new RegraNegocioException("O diagnóstico só pode ser iniciado em uma OS recém-recebida.");
        }
        registrarTransicao(StatusOrdemServico.EM_DIAGNOSTICO, Horario.agora());
    }

    public void anexarOrcamento(Orcamento orcamento) {
        if (this.status != StatusOrdemServico.EM_DIAGNOSTICO) {
            throw new RegraNegocioException("O orçamento só pode ser anexado após a fase de diagnóstico.");
        }

        this.orcamentos.add(orcamento);
        registrarTransicao(StatusOrdemServico.AGUARDANDO_APROVACAO, Horario.agora());
    }

    public void aprovarOrcamento() {
        if (this.status != StatusOrdemServico.AGUARDANDO_APROVACAO) {
            throw new RegraNegocioException("Apenas ordens aguardando aprovação podem ser aprovadas.");
        }
        registrarTransicao(StatusOrdemServico.APROVADA, Horario.agora());
    }

    public void recusarOrcamento() {
        if (this.status != StatusOrdemServico.AGUARDANDO_APROVACAO) {
            throw new RegraNegocioException("Apenas ordens aguardando aprovação podem ser recusadas.");
        }
        registrarTransicao(StatusOrdemServico.RECUSADA, Horario.agora());
    }

    public void cancelar() {
        if (!podeSerCancelada()) {
            throw new RegraNegocioException("Uma OS com status " + this.status + " não pode ser cancelada.");
        }
        registrarTransicao(StatusOrdemServico.CANCELADA, Horario.agora());
    }

    public void iniciarExecucao() {
        if (this.status != StatusOrdemServico.APROVADA) {
            throw new RegraNegocioException("A execução só pode ser iniciada em uma OS aprovada.");
        }
        registrarTransicao(StatusOrdemServico.EM_EXECUCAO, Horario.agora());
    }

    public void finalizar() {
        if (this.status != StatusOrdemServico.EM_EXECUCAO) {
            throw new RegraNegocioException("Apenas uma OS em execução pode ser finalizada.");
        }
        this.dataConclusao = Horario.agora();
        registrarTransicao(StatusOrdemServico.FINALIZADA, this.dataConclusao);
    }

    public void entregar() {
        if (this.status != StatusOrdemServico.FINALIZADA) {
            throw new RegraNegocioException("Apenas uma OS finalizada pode ser entregue.");
        }
        registrarTransicao(StatusOrdemServico.ENTREGUE, Horario.agora());
    }

    public List<TransicaoStatus> getNovasTransicoes() {
        return Collections.unmodifiableList(novasTransicoes);
    }

    private void registrarTransicao(StatusOrdemServico novoStatus, LocalDateTime momento) {
        this.novasTransicoes.add(new TransicaoStatus(UuidCreator.getTimeOrderedEpoch(), this.status, novoStatus, momento));
        this.status = novoStatus;
    }

    private boolean podeSerCancelada() {
        return this.status == StatusOrdemServico.RECEBIDA
                || this.status == StatusOrdemServico.EM_DIAGNOSTICO
                || this.status == StatusOrdemServico.AGUARDANDO_APROVACAO;
    }
    
    private void validarRelatoProblema(String relato) {
        if (relato == null || relato.isBlank()) {
            throw new RegraNegocioException("O relato do problema não pode ser nulo ou vazio.");
        }

        if (relato.length() < 10 || relato.length() > 500) {
            throw new RegraNegocioException("O relato do problema deve ter entre 10 e 500 caracteres.");
        }
    }
}