package br.com.fiap.oficina_mecanica.estoque.application.usecase;

import br.com.fiap.oficina_mecanica.compartilhado.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.oficina_mecanica.compartilhado.domain.exception.RegraNegocioException;
import br.com.fiap.oficina_mecanica.estoque.application.dto.AjustarQuantidadeInput;
import br.com.fiap.oficina_mecanica.estoque.application.dto.EstoqueOutput;
import br.com.fiap.oficina_mecanica.estoque.domain.ProdutoEstoque;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReservarPecaUseCaseTest {

    private final ProdutoEstoqueRepositoryEmMemoria repository = new ProdutoEstoqueRepositoryEmMemoria();

    @Test
    void reservaPecasComSucesso() {
        UUID id = UUID.randomUUID();
        ProdutoEstoque produto = new ProdutoEstoque(id, "DISCO DE FREIO", 10, new BigDecimal("200.00"));
        repository.salvar(produto);

        ReservarPecaUseCase useCase = new ReservarPecaUseCase(repository);
        AjustarQuantidadeInput input = new AjustarQuantidadeInput(3);

        EstoqueOutput output = useCase.execute(id, input);

        assertThat(output.quantidadeDisponivel()).isEqualTo(7);
        assertThat(output.quantidadeReservada()).isEqualTo(3);
    }

    @Test
    void reservaComEstoqueInsuficienteLancaRegraNegocioException() {
        UUID id = UUID.randomUUID();
        ProdutoEstoque produto = new ProdutoEstoque(id, "DISCO DE FREIO", 2, new BigDecimal("200.00"));
        repository.salvar(produto);

        ReservarPecaUseCase useCase = new ReservarPecaUseCase(repository);
        AjustarQuantidadeInput input = new AjustarQuantidadeInput(5);

        assertThatThrownBy(() -> useCase.execute(id, input))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Estoque insuficiente para a peça: DISCO DE FREIO");
    }

    @Test
    void reservaDeProdutoInexistenteLancaRecursoNaoEncontrado() {
        UUID idInexistente = UUID.randomUUID();
        ReservarPecaUseCase useCase = new ReservarPecaUseCase(repository);

        assertThatThrownBy(() -> useCase.execute(idInexistente, new AjustarQuantidadeInput(1)))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Peça não encontrada no estoque com o ID: " + idInexistente);
    }
}