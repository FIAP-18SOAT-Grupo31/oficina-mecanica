package br.com.fiap.oficina_mecanica.estoque.domain;

import br.com.fiap.oficina_mecanica.compartilhado.domain.exception.RegraNegocioException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ProdutoEstoque {

    private UUID id;
    private String nomePeca;
    private int quantidadeDisponivel;
    private int quantidadeReservada;
    private BigDecimal valorUnitario;

    public ProdutoEstoque(UUID id, String nomePeca, int quantidadeInicial, BigDecimal valorUnitario) {
        if (quantidadeInicial < 0) throw new RegraNegocioException("Quantidade inicial não pode ser negativa.");
        if (valorUnitario == null || valorUnitario.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraNegocioException("Valor unitário deve ser maior que zero.");
        }

        this.id = id;
        this.nomePeca = nomePeca;
        this.quantidadeDisponivel = quantidadeInicial;
        this.quantidadeReservada = 0;
        this.valorUnitario = valorUnitario;
    }

    public void reservar(int quantidade) {
        if (quantidade <= 0) throw new RegraNegocioException("Quantidade de reserva deve ser maior que zero.");
        if (this.quantidadeDisponivel < quantidade) throw new RegraNegocioException("Estoque insuficiente para a peça: " + nomePeca);

        this.quantidadeDisponivel -= quantidade;
        this.quantidadeReservada += quantidade;
    }

    public void confirmarBaixa(int quantidade) {
        if (this.quantidadeReservada < quantidade) throw new RegraNegocioException("Quantidade reservada insuficiente para confirmar baixa.");

        this.quantidadeReservada -= quantidade;
    }

    public void cancelarReserva(int quantidade) {
        if (this.quantidadeReservada < quantidade) throw new RegraNegocioException("Quantidade reservada insuficiente para cancelamento.");

        this.quantidadeReservada -= quantidade;
        this.quantidadeDisponivel += quantidade;
    }

    public void adicionarEstoque(int quantidade) {
        if (quantidade <= 0) throw new RegraNegocioException("Quantidade de entrada deve ser maior que zero.");
        this.quantidadeDisponivel += quantidade;
    }

    public void removerEstoque(int quantidade) {
        if (quantidade <= 0)
            throw new RegraNegocioException("Quantidade de saída deve ser maior que zero.");

        if (this.quantidadeDisponivel < quantidade)
            throw new RegraNegocioException("Quantidade disponível insuficiente para remoção do estoque.");

        this.quantidadeDisponivel -= quantidade;
    }
}