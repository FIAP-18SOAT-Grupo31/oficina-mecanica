package br.com.fiap.oficina_mecanica.ordemservico.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Orcamento {
    private List<ItemServico> servicos = new ArrayList<>();
    private List<ItemPeca> pecas = new ArrayList<>();
    private LocalDateTime dataValidade;

    public Orcamento(LocalDateTime dataValidade) {
        this.dataValidade = dataValidade;
    }

    public void adicionarServico(ItemServico servico) {
        this.servicos.add(servico);
    }

    public void adicionarPeca(ItemPeca peca) {
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
}