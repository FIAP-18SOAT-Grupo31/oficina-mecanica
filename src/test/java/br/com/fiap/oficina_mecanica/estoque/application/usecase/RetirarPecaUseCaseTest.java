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

class RetirarPecaUseCaseTest {

    private final ProdutoEstoqueRepositoryEmMemoria repository = new ProdutoEstoqueRepositoryEmMemoria();

    @Test
    void confirmaBaixaDePecaReservadaComSucesso() {
        UUID id = UUID.randomUUID();
        ProdutoEstoque produto = new ProdutoEstoque(id, "VELA DE IGNIÇÃO", 10, new BigDecimal("30.00"));
        produto.reservar(4); // Disponível: 6, Reservado: 4
        repository.salvar(produto);

        RetirarPecaUseCase useCase = new RetirarPecaUseCase(repository);
        AjustarQuantidadeInput input = new AjustarQuantidadeInput(4);

        EstoqueOutput output = useCase.execute(id, input);

        assertThat(output.quantidadeDisponivel()).isEqualTo(6);
        assertThat(output.quantidadeReservada()).isEqualTo(0);
    }

    @Test
    void baixaSemQuantidadeReservadaSuficienteLancaRegraNegocioException() {
        UUID id = UUID.randomUUID();
        ProdutoEstoque produto = new ProdutoEstoque(id, "VELA DE IGNIÇÃO", 10, new BigDecimal("30.00"));
        produto.reservar(2); // Reservado: 2
        repository.salvar(produto);

        RetirarPecaUseCase useCase = new RetirarPecaUseCase(repository);

        assertThatThrownBy(() -> useCase.execute(id, new AjustarQuantidadeInput(5)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Quantidade reservada insuficiente para confirmar baixa.");
    }

    @Test
    void retiradaDeProdutoInexistenteLancaRecursoNaoEncontrado() {
        UUID idInexistente = UUID.randomUUID();
        RetirarPecaUseCase useCase = new RetirarPecaUseCase(repository);

        assertThatThrownBy(() -> useCase.execute(idInexistente, new AjustarQuantidadeInput(1)))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Peça não encontrada no estoque com o ID: " + idInexistente);
    }
}