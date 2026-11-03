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

class RemoverEstoqueUseCaseTest {

    private final ProdutoEstoqueRepositoryEmMemoria repository = new ProdutoEstoqueRepositoryEmMemoria();

    @Test
    void removeEstoqueComSucesso() {
        UUID id = UUID.randomUUID();
        ProdutoEstoque produto = new ProdutoEstoque(id, "VELA DE IGNIÇÃO", 10, new BigDecimal("35.00"));
        repository.salvar(produto);

        RemoverEstoqueUseCase useCase = new RemoverEstoqueUseCase(repository);
        AjustarQuantidadeInput input = new AjustarQuantidadeInput(4);

        EstoqueOutput output = useCase.execute(id, input);

        assertThat(output.quantidadeDisponivel()).isEqualTo(6);
        assertThat(output.quantidadeReservada()).isEqualTo(0);
    }

    @Test
    void removeEstoqueComQuantidadeMaiorQueDisponivelLancaRegraNegocioException() {
        UUID id = UUID.randomUUID();
        ProdutoEstoque produto = new ProdutoEstoque(id, "VELA DE IGNIÇÃO", 3, new BigDecimal("35.00"));
        repository.salvar(produto);

        RemoverEstoqueUseCase useCase = new RemoverEstoqueUseCase(repository);
        AjustarQuantidadeInput input = new AjustarQuantidadeInput(5);

        assertThatThrownBy(() -> useCase.execute(id, input))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Quantidade disponível insuficiente para remoção do estoque.");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -2})
    void removeEstoqueComQuantidadeInvalidaLancaRegraNegocioException(int quantidadeInvalida) {
        UUID id = UUID.randomUUID();
        ProdutoEstoque produto = new ProdutoEstoque(id, "VELA DE IGNIÇÃO", 10, new BigDecimal("35.00"));
        repository.salvar(produto);

        RemoverEstoqueUseCase useCase = new RemoverEstoqueUseCase(repository);
        AjustarQuantidadeInput input = new AjustarQuantidadeInput(quantidadeInvalida);

        assertThatThrownBy(() -> useCase.execute(id, input))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Quantidade de saída deve ser maior que zero.");
    }

    @Test
    void removeEstoqueDeProdutoInexistenteLancaRecursoNaoEncontrado() {
        UUID idInexistente = UUID.randomUUID();
        RemoverEstoqueUseCase useCase = new RemoverEstoqueUseCase(repository);

        assertThatThrownBy(() -> useCase.execute(idInexistente, new AjustarQuantidadeInput(1)))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Peça não encontrada no estoque com o ID: " + idInexistente);
    }
}