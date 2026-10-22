package br.com.fiap.oficina_mecanica.estoque.application.usecase;

import br.com.fiap.oficina_mecanica.compartilhado.domain.exception.RegraNegocioException;
import br.com.fiap.oficina_mecanica.estoque.application.dto.CriarProdutoEstoqueInput;
import br.com.fiap.oficina_mecanica.estoque.application.dto.EstoqueOutput;
import br.com.fiap.oficina_mecanica.estoque.domain.ProdutoEstoque;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CriarProdutoEstoqueUseCaseTest {

    private final ProdutoEstoqueRepositoryEmMemoria repository = new ProdutoEstoqueRepositoryEmMemoria();

    @Test
    void criaNovoProdutoComSucesso() {
        CriarProdutoEstoqueUseCase useCase = new CriarProdutoEstoqueUseCase(repository);
        CriarProdutoEstoqueInput input = new CriarProdutoEstoqueInput("AMORTECEDOR DIANTEIRO", new BigDecimal("350.00"), 5);

        EstoqueOutput output = useCase.execute(input);

        ProdutoEstoque salvo = repository.buscarPorId(output.id()).orElseThrow();
        assertThat(salvo.getNomePeca()).isEqualTo("AMORTECEDOR DIANTEIRO");
        assertThat(salvo.getQuantidadeDisponivel()).isEqualTo(5);
        assertThat(salvo.getQuantidadeReservada()).isEqualTo(0);
        assertThat(salvo.getValorUnitario()).isEqualTo(new BigDecimal("350.00"));
    }

    @Test
    void produtoDuplicadoLancaRegraNegocioException() {
        ProdutoEstoque existente = new ProdutoEstoque(UUID.randomUUID(), "AMORTECEDOR DIANTEIRO", 10, new BigDecimal("350.00"));
        repository.salvar(existente);

        CriarProdutoEstoqueUseCase useCase = new CriarProdutoEstoqueUseCase(repository);
        CriarProdutoEstoqueInput input = new CriarProdutoEstoqueInput("AMORTECEDOR DIANTEIRO", new BigDecimal("350.00"), 5);

        assertThatThrownBy(() -> useCase.execute(input))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Já existe uma peça cadastrada no estoque com o nome: AMORTECEDOR DIANTEIRO");
    }
}