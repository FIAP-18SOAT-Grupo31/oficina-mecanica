package br.com.fiap.oficina_mecanica.estoque.application.usecase;

import br.com.fiap.oficina_mecanica.compartilhado.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.oficina_mecanica.compartilhado.domain.exception.RegraNegocioException;
import br.com.fiap.oficina_mecanica.estoque.application.dto.AjustarQuantidadeInput;
import br.com.fiap.oficina_mecanica.estoque.application.dto.EstoqueOutput;
import br.com.fiap.oficina_mecanica.estoque.domain.ProdutoEstoque;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AdicionarEstoqueUseCaseTest {

    private final ProdutoEstoqueRepositoryEmMemoria repository = new ProdutoEstoqueRepositoryEmMemoria();

    @Test
    void adicionaEstoqueComSucesso() {
        UUID id = UUID.randomUUID();
        ProdutoEstoque produto = new ProdutoEstoque(id, "FILTRO DE AR", 10, new BigDecimal("50.00"));
        repository.salvar(produto);

        AdicionarEstoqueUseCase useCase = new AdicionarEstoqueUseCase(repository);
        AjustarQuantidadeInput input = new AjustarQuantidadeInput(5);

        EstoqueOutput output = useCase.execute(id, input);

        assertThat(output.quantidadeDisponivel()).isEqualTo(15);
        assertThat(output.quantidadeReservada()).isEqualTo(0);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -5})
    void adicionaEstoqueComQuantidadeInvalidaLancaRegraNegocioException(int quantidadeInvalida) {
        UUID id = UUID.randomUUID();
        ProdutoEstoque produto = new ProdutoEstoque(id, "FILTRO DE AR", 10, new BigDecimal("50.00"));
        repository.salvar(produto);

        AdicionarEstoqueUseCase useCase = new AdicionarEstoqueUseCase(repository);
        AjustarQuantidadeInput input = new AjustarQuantidadeInput(quantidadeInvalida);

        assertThatThrownBy(() -> useCase.execute(id, input))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Quantidade de entrada deve ser maior que zero.");
    }

    @Test
    void adicionaEstoqueEmProdutoInexistenteLancaRecursoNaoEncontrado() {
        UUID idInexistente = UUID.randomUUID();
        AdicionarEstoqueUseCase useCase = new AdicionarEstoqueUseCase(repository);

        assertThatThrownBy(() -> useCase.execute(idInexistente, new AjustarQuantidadeInput(5)))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Peça não encontrada no estoque com o ID: " + idInexistente);
    }
}