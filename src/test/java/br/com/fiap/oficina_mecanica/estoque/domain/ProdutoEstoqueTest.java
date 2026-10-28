package br.com.fiap.oficina_mecanica.estoque.domain;

import br.com.fiap.oficina_mecanica.compartilhado.domain.exception.RegraNegocioException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProdutoEstoqueTest {

    private ProdutoEstoque criarProdutoPadrao() {
        return new ProdutoEstoque(UUID.randomUUID(), "Filtro de Óleo", 10, new BigDecimal("25.50"));
    }

    @Test
    void criaProdutoComSucesso() {
        ProdutoEstoque produto = criarProdutoPadrao();

        assertThat(produto.getNomePeca()).isEqualTo("Filtro de Óleo");
        assertThat(produto.getQuantidadeDisponivel()).isEqualTo(10);
        assertThat(produto.getQuantidadeReservada()).isZero();
        assertThat(produto.getValorUnitario()).isEqualByComparingTo("25.50");
    }

    @Test
    void naoCriaProdutoComQuantidadeNegativaOuValorInvalido() {
        UUID id = UUID.randomUUID();

        assertThatThrownBy(() -> new ProdutoEstoque(id, "Peca", -1, new BigDecimal("10.00")))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Quantidade inicial não pode ser negativa.");

        assertThatThrownBy(() -> new ProdutoEstoque(id, "Peca", 5, BigDecimal.ZERO))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Valor unitário deve ser maior que zero.");

        assertThatThrownBy(() -> new ProdutoEstoque(id, "Peca", 5, null))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Valor unitário deve ser maior que zero.");
    }

    @Test
    void reservaQuantidadeDisponivelComSucesso() {
        ProdutoEstoque produto = criarProdutoPadrao();

        produto.reservar(4);

        assertThat(produto.getQuantidadeDisponivel()).isEqualTo(6);
        assertThat(produto.getQuantidadeReservada()).isEqualTo(4);
    }

    @Test
    void falhaAoTentarReservarMaisQueODisponivel() {
        ProdutoEstoque produto = criarProdutoPadrao();

        assertThatThrownBy(() -> produto.reservar(11))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("Estoque insuficiente");
    }

    @Test
    void falhaAoReservarComQuantidadeMenorOuIgualAZero() {
        ProdutoEstoque produto = criarProdutoPadrao();

        assertThatThrownBy(() -> produto.reservar(0))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Quantidade de reserva deve ser maior que zero.");

        assertThatThrownBy(() -> produto.reservar(-1))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Quantidade de reserva deve ser maior que zero.");
    }

    @Test
    void confirmaBaixaDeProdutoReservado() {
        ProdutoEstoque produto = criarProdutoPadrao();
        produto.reservar(3);

        produto.confirmarBaixa(2);

        assertThat(produto.getQuantidadeDisponivel()).isEqualTo(7);
        assertThat(produto.getQuantidadeReservada()).isEqualTo(1);
    }

    @Test
    void falhaAoConfirmarBaixaSeQuantidadeReservadaInsuficiente() {
        ProdutoEstoque produto = criarProdutoPadrao();
        produto.reservar(2);

        assertThatThrownBy(() -> produto.confirmarBaixa(3))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Quantidade reservada insuficiente para confirmar baixa.");
    }

    @Test
    void cancelaReservaEVoltaParaODisponivel() {
        ProdutoEstoque produto = criarProdutoPadrao();
        produto.reservar(5);

        produto.cancelarReserva(5);

        assertThat(produto.getQuantidadeDisponivel()).isEqualTo(10);
        assertThat(produto.getQuantidadeReservada()).isZero();
    }

    @Test
    void falhaAoCancelarReservaSeQuantidadeReservadaInsuficiente() {
        ProdutoEstoque produto = criarProdutoPadrao();
        produto.reservar(2);

        assertThatThrownBy(() -> produto.cancelarReserva(3))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Quantidade reservada insuficiente para cancelamento.");
    }

    @Test
    void adicionaEstoqueComSucesso() {
        ProdutoEstoque produto = criarProdutoPadrao();

        produto.adicionarEstoque(5);

        assertThat(produto.getQuantidadeDisponivel()).isEqualTo(15);
    }

    @Test
    void falhaAoAdicionarEstoqueComQuantidadeMenorOuIgualAZero() {
        ProdutoEstoque produto = criarProdutoPadrao();

        assertThatThrownBy(() -> produto.adicionarEstoque(0))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Quantidade de entrada deve ser maior que zero.");

        assertThatThrownBy(() -> produto.adicionarEstoque(-2))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Quantidade de entrada deve ser maior que zero.");
    }

    @Test
    void removeEstoqueComSucesso() {
        ProdutoEstoque produto = criarProdutoPadrao();

        produto.removerEstoque(4);

        assertThat(produto.getQuantidadeDisponivel()).isEqualTo(6);
        assertThat(produto.getQuantidadeReservada()).isZero();
    }

    @Test
    void falhaAoRemoverEstoqueComQuantidadeMaiorQueDisponivel() {
        ProdutoEstoque produto = criarProdutoPadrao();

        assertThatThrownBy(() -> produto.removerEstoque(11))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Quantidade disponível insuficiente para remoção do estoque.");
    }

    @Test
    void falhaAoRemoverEstoqueComQuantidadeMenorOuIgualAZero() {
        ProdutoEstoque produto = criarProdutoPadrao();

        assertThatThrownBy(() -> produto.removerEstoque(0))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Quantidade de saída deve ser maior que zero.");

        assertThatThrownBy(() -> produto.removerEstoque(-3))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Quantidade de saída deve ser maior que zero.");
    }
}