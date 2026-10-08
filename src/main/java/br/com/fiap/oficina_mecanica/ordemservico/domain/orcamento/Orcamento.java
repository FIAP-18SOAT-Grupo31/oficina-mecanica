package br.com.fiap.oficina_mecanica.ordemservico.domain.orcamento;

import br.com.fiap.oficina_mecanica.compartilhado.domain.Horario;
import br.com.fiap.oficina_mecanica.compartilhado.domain.exception.RegraNegocioException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Orcamento {
    private UUID id;
    private UUID ordemServicoId;
    private List<ItemServico> servicos;
    private List<ItemPeca> pecas;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataValidade;
    private StatusOrcamento status;

    public Orcamento(UUID id, UUID ordemServicoId, LocalDateTime dataValidade) {
        if (id == null) {
            throw new IllegalArgumentException("O ID do orçamento não pode ser nulo.");
        }
        this.id = id;
        this.ordemServicoId = ordemServicoId;
        this.servicos = new ArrayList<>();
        this.pecas = new ArrayList<>();
        this.dataCriacao = Horario.agora();
        this.dataValidade = dataValidade;
        this.status = StatusOrcamento.PENDENTE;
    }

    public Orcamento(UUID id, UUID ordemServicoId, LocalDateTime dataCriacao, LocalDateTime dataValidade, StatusOrcamento status, List<ItemServico> servicos, List<ItemPeca> pecas) {
        this.id = id;
        this.ordemServicoId = ordemServicoId;
        this.dataCriacao = dataCriacao;
        this.dataValidade = dataValidade;
        this.status = status;
        this.servicos = new ArrayList<>(servicos);
        this.pecas = new ArrayList<>(pecas);
    }

    public void adicionarServico(ItemServico servico) {
        validarModificacao();
        this.servicos.add(servico);
    }

    public void adicionarPeca(ItemPeca peca) {
        validarModificacao();
        this.pecas.add(peca);
    }

    public BigDecimal calcularValorTotal() {
        BigDecimal totalServicos = servicos.stream()
                .map(ItemServico::valorMaoDeObra)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPecas = pecas.stream()
                .map(ItemPeca::calcularTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return totalServicos.add(totalPecas);
    }

    public void aprovar() {
        if (this.status != StatusOrcamento.PENDENTE) {
            throw new RegraNegocioException("Apenas orçamentos pendentes podem ser aprovados.");
        }
        if (Horario.agora().isAfter(this.dataValidade)) {
            this.status = StatusOrcamento.EXPIRADO;
            throw new RegraNegocioException("O orçamento está expirado e não pode ser aprovado.");
        }
        this.status = StatusOrcamento.APROVADO;
    }

    public void rejeitar() {
        if (this.status != StatusOrcamento.PENDENTE) {
            throw new RegraNegocioException("Apenas orçamentos pendentes podem ser rejeitados.");
        }
        this.status = StatusOrcamento.REJEITADO;
    }

    private void validarModificacao() {
        if (this.status != StatusOrcamento.PENDENTE) {
            throw new RegraNegocioException("Não é possível alterar um orçamento que já foi " + this.status);
        }
    }

    public UUID getId() { return id; }
    public UUID getOrdemServicoId() { return ordemServicoId; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public LocalDateTime getDataValidade() { return dataValidade; }
    public StatusOrcamento getStatus() { return status; }
    public List<ItemServico> getServicos() { return Collections.unmodifiableList(servicos); }
    public List<ItemPeca> getPecas() { return Collections.unmodifiableList(pecas); }
}