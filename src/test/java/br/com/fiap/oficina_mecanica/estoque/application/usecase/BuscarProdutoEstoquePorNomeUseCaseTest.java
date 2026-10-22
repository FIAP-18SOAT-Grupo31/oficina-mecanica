package br.com.fiap.oficina_mecanica.estoque.application.usecase;
import br.com.fiap.oficina_mecanica.compartilhado.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.oficina_mecanica.estoque.application.dto.EstoqueOutput;
import br.com.fiap.oficina_mecanica.estoque.domain.ProdutoEstoque;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BuscarProdutoEstoquePorNomeUseCaseTest {

    private final ProdutoEstoqueRepositoryEmMemoria repository = new ProdutoEstoqueRepositoryEmMemoria();

    @Test
    void buscaProdutoExistentePorNomeComSucesso() {
        ProdutoEstoque produto = new ProdutoEstoque(UUID.randomUUID(), "PASTILHA DE FREIO", 20, new BigDecimal("120.00"));
        repository.salvar(produto);

        BuscarProdutoEstoquePorNomeUseCase useCase = new BuscarProdutoEstoquePorNomeUseCase(repository);
        EstoqueOutput output = useCase.execute("PASTILHA DE FREIO");

        assertThat(output.nomePeca()).isEqualTo("PASTILHA DE FREIO");
        assertThat(output.quantidadeDisponivel()).isEqualTo(20);
    }

    @Test
    void produtoInexistentePorNomeLancaRecursoNaoEncontrado() {
        BuscarProdutoEstoquePorNomeUseCase useCase = new BuscarProdutoEstoquePorNomeUseCase(repository);

        assertThatThrownBy(() -> useCase.execute("VELA DE VENTO"))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Peça não encontrada no estoque com o nome: VELA DE VENTO");
    }

    @Test
    void nomeInvalidoLancaIllegalArgumentException() {
        BuscarProdutoEstoquePorNomeUseCase useCase = new BuscarProdutoEstoquePorNomeUseCase(repository);

        assertThatThrownBy(() -> useCase.execute("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("O nome da peça não pode ser nulo ou vazio.");
    }
}