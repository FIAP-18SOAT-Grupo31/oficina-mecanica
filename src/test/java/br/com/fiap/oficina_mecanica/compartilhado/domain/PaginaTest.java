package br.com.fiap.oficina_mecanica.compartilhado.domain;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaginaTest {

    @Test
    void guardaOsDadosDaPagina() {
        Pagina<String> pagina = new Pagina<>(List.of("a", "b"), 1, 2, 5);

        assertThat(pagina.conteudo()).containsExactly("a", "b");
        assertThat(pagina.pagina()).isEqualTo(1);
        assertThat(pagina.tamanho()).isEqualTo(2);
        assertThat(pagina.totalElementos()).isEqualTo(5);
    }

    @Test
    void aceitaPaginaVaziaNoLimiteDosValoresValidos() {
        Pagina<String> pagina = new Pagina<>(List.of(), 0, 1, 0);

        assertThat(pagina.conteudo()).isEmpty();
        assertThat(pagina.totalPaginas()).isZero();
    }

    @Test
    void copiaOConteudoParaNaoSofrerAlteracoesExternas() {
        List<String> original = new ArrayList<>(List.of("a"));

        Pagina<String> pagina = new Pagina<>(original, 0, 10, 1);
        original.add("b");

        assertThat(pagina.conteudo()).containsExactly("a");
        assertThatThrownBy(() -> pagina.conteudo().add("c")).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void calculaOTotalDePaginasArredondandoParaCima() {
        assertThat(new Pagina<>(List.of(), 0, 10, 0).totalPaginas()).isZero();
        assertThat(new Pagina<>(List.of(), 0, 10, 1).totalPaginas()).isEqualTo(1);
        assertThat(new Pagina<>(List.of(), 0, 10, 10).totalPaginas()).isEqualTo(1);
        assertThat(new Pagina<>(List.of(), 0, 10, 11).totalPaginas()).isEqualTo(2);
        assertThat(new Pagina<>(List.of(), 0, 1, 3).totalPaginas()).isEqualTo(3);
    }

    @Test
    void mapeiaOConteudoMantendoOsDadosDePaginacao() {
        Pagina<Integer> pagina = new Pagina<>(List.of(1, 2), 3, 2, 8);

        Pagina<String> mapeada = pagina.mapear(numero -> "n" + numero);

        assertThat(mapeada.conteudo()).containsExactly("n1", "n2");
        assertThat(mapeada.pagina()).isEqualTo(3);
        assertThat(mapeada.tamanho()).isEqualTo(2);
        assertThat(mapeada.totalElementos()).isEqualTo(8);
    }

    @Test
    void rejeitaConteudoNulo() {
        assertThatThrownBy(() -> new Pagina<String>(null, 0, 10, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("O conteúdo da página é inválido.");
    }

    @Test
    void rejeitaNumeroDePaginaNegativo() {
        assertThatThrownBy(() -> new Pagina<>(List.of(), -1, 10, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("O número da página não pode ser negativo.");
    }

    @Test
    void rejeitaTamanhoMenorQueUm() {
        assertThatThrownBy(() -> new Pagina<>(List.of(), 0, 0, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("O tamanho da página deve ser maior que zero.");
    }

    @Test
    void rejeitaTotalDeElementosNegativo() {
        assertThatThrownBy(() -> new Pagina<>(List.of(), 0, 10, -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("O total de elementos não pode ser negativo.");
    }
}
