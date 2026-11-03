package br.com.fiap.oficina_mecanica.estoque.application.usecase;

import br.com.fiap.oficina_mecanica.compartilhado.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.oficina_mecanica.estoque.application.dto.EstoqueOutput;
import br.com.fiap.oficina_mecanica.estoque.domain.ProdutoEstoque;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BuscarProdutoEstoquePorIdUseCaseTest {

    private final ProdutoEstoqueRepositoryEmMemoria repository = new ProdutoEstoqueRepositoryEmMemoria();

    @Test
    void buscaProdutoExistentePorIdComSucesso() {
        UUID id = UUID.randomUUID();
        ProdutoEstoque produto = new ProdutoEstoque(id, "FILTRO DE ÓLEO", 10, new BigDecimal("45.00"));
        repository.salvar(produto);

        BuscarProdutoEstoquePorIdUseCase useCase = new BuscarProdutoEstoquePorIdUseCase(repository);
        EstoqueOutput output = useCase.execute(id);

        assertThat(output.id()).isEqualTo(id);
        assertThat(output.nomePeca()).isEqualTo("FILTRO DE ÓLEO");
        assertThat(output.quantidadeDisponivel()).isEqualTo(10);
        assertThat(output.quantidadeReservada()).isEqualTo(0);
        assertThat(output.valorUnitario()).isEqualTo(new BigDecimal("45.00"));
    }

    @Test
    void produtoInexistenteLancaRecursoNaoEncontrado() {
        UUID idInexistente = UUID.randomUUID();
        BuscarProdutoEstoquePorIdUseCase useCase = new BuscarProdutoEstoquePorIdUseCase(repository);

        assertThatThrownBy(() -> useCase.execute(idInexistente))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Peça não encontrada no estoque com o ID: " + idInexistente);
    }
}