package br.com.fiap.oficina_mecanica.estoque.application.usecase;

import br.com.fiap.oficina_mecanica.compartilhado.application.dto.PaginaOutput;
import br.com.fiap.oficina_mecanica.compartilhado.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.oficina_mecanica.compartilhado.domain.exception.RegraNegocioException;
import br.com.fiap.oficina_mecanica.estoque.application.dto.AtualizarProdutoEstoqueInput;
import br.com.fiap.oficina_mecanica.estoque.application.dto.EstoqueOutput;
import br.com.fiap.oficina_mecanica.estoque.domain.ProdutoEstoque;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GestaoProdutoEstoqueUseCasesTest {

    private final ProdutoEstoqueRepositoryEmMemoria repository = new ProdutoEstoqueRepositoryEmMemoria();

    private final ListarProdutosEstoqueUseCase listar = new ListarProdutosEstoqueUseCase(repository);
    private final AtualizarProdutoEstoqueUseCase atualizar = new AtualizarProdutoEstoqueUseCase(repository);
    private final ExcluirProdutoEstoqueUseCase excluir = new ExcluirProdutoEstoqueUseCase(repository);

    private ProdutoEstoque cadastrar(String nome) {
        return repository.salvar(new ProdutoEstoque(UUID.randomUUID(), nome, 10, new BigDecimal("50.00")));
    }

    @Test
    void listaPecasPaginadasEmOrdemDeNome() {
        cadastrar("VELA");
        cadastrar("FILTRO DE AR");
        cadastrar("FILTRO DE OLEO");

        PaginaOutput<EstoqueOutput> pagina = listar.execute(null, 0, 2);

        assertThat(pagina.conteudo()).extracting(EstoqueOutput::nomePeca)
                .containsExactly("FILTRO DE AR", "FILTRO DE OLEO");
        assertThat(pagina.totalElementos()).isEqualTo(3);
        assertThat(pagina.totalPaginas()).isEqualTo(2);
    }

    @Test
    void listaFiltrandoPorParteDoNomeEIgnoraFiltroEmBranco() {
        cadastrar("VELA");
        cadastrar("FILTRO DE AR");

        assertThat(listar.execute("filtro", 0, 20).conteudo()).extracting(EstoqueOutput::nomePeca)
                .containsExactly("FILTRO DE AR");
        assertThat(listar.execute("   ", 0, 20).totalElementos()).isEqualTo(2);
    }

    @Test
    void atualizaNomeEValorDaPeca() {
        ProdutoEstoque produto = cadastrar("FILTRO DE AR");

        EstoqueOutput output = atualizar.execute(produto.getId(),
                new AtualizarProdutoEstoqueInput("  filtro de ar esportivo ", new BigDecimal("80.00")));

        assertThat(output.nomePeca()).isEqualTo("FILTRO DE AR ESPORTIVO");
        assertThat(output.valorUnitario()).isEqualByComparingTo("80.00");
        assertThat(output.quantidadeDisponivel()).isEqualTo(10);
    }

    @Test
    void atualizaMantendoOMesmoNome() {
        ProdutoEstoque produto = cadastrar("FILTRO DE AR");

        EstoqueOutput output = atualizar.execute(produto.getId(),
                new AtualizarProdutoEstoqueInput("FILTRO DE AR", new BigDecimal("55.00")));

        assertThat(output.valorUnitario()).isEqualByComparingTo("55.00");
    }

    @Test
    void naoAtualizaParaNomeDeOutraPeca() {
        cadastrar("VELA");
        ProdutoEstoque produto = cadastrar("FILTRO DE AR");

        assertThatThrownBy(() -> atualizar.execute(produto.getId(),
                new AtualizarProdutoEstoqueInput("vela", new BigDecimal("55.00"))))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Já existe uma peça cadastrada no estoque com o nome: VELA");
    }

    @Test
    void atualizarPecaInexistenteLancaRecursoNaoEncontrado() {
        UUID idInexistente = UUID.randomUUID();

        assertThatThrownBy(() -> atualizar.execute(idInexistente,
                new AtualizarProdutoEstoqueInput("VELA", new BigDecimal("10.00"))))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Peça não encontrada no estoque com o ID: " + idInexistente);
    }

    @Test
    void excluiPecaSemReserva() {
        ProdutoEstoque produto = cadastrar("VELA");

        excluir.execute(produto.getId());

        assertThat(repository.buscarPorId(produto.getId())).isEmpty();
    }

    @Test
    void naoExcluiPecaComReservaEmOrcamento() {
        ProdutoEstoque produto = cadastrar("VELA");
        produto.reservar(2);

        assertThatThrownBy(() -> excluir.execute(produto.getId()))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Não é possível excluir a peça: há unidades reservadas em orçamento pendente.");
        assertThat(repository.buscarPorId(produto.getId())).isPresent();
    }

    @Test
    void excluirPecaInexistenteLancaRecursoNaoEncontrado() {
        UUID idInexistente = UUID.randomUUID();

        assertThatThrownBy(() -> excluir.execute(idInexistente))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }
}
