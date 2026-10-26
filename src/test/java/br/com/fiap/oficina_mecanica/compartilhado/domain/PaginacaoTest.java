package br.com.fiap.oficina_mecanica.compartilhado.domain;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaginacaoTest {

    @Test
    void aceitaPaginaZeroETamanhoNosLimites() {
        assertThat(new Paginacao(0, 1)).isEqualTo(new Paginacao(0, 1));
        assertThat(new Paginacao(5, Paginacao.TAMANHO_MAXIMO).tamanho()).isEqualTo(100);
    }

    @Test
    void paginaNegativaEhRecusada() {
        assertThatThrownBy(() -> new Paginacao(-1, 10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A página não pode ser negativa.");
    }

    @Test
    void tamanhoForaDoLimiteEhRecusado() {
        assertThatThrownBy(() -> new Paginacao(0, 0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Paginacao(0, 101))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("O tamanho da página deve estar entre 1 e 100.");
    }

    @Test
    void paginaCalculaTotalDePaginasArredondandoParaCima() {
        Pagina<String> pagina = Pagina.de(List.of("a", "b"), new Paginacao(0, 2), 5);

        assertThat(pagina.totalPaginas()).isEqualTo(3);
        assertThat(pagina.totalElementos()).isEqualTo(5);
        assertThat(pagina.pagina()).isZero();
        assertThat(pagina.tamanho()).isEqualTo(2);
    }

    @Test
    void paginaVaziaTemZeroPaginas() {
        Pagina<String> pagina = Pagina.de(List.of(), new Paginacao(0, 10), 0);

        assertThat(pagina.conteudo()).isEmpty();
        assertThat(pagina.totalPaginas()).isZero();
    }

    @Test
    void mapearConverteConteudoMantendoMetadados() {
        Pagina<Integer> pagina = Pagina.de(List.of(1, 2), new Paginacao(1, 2), 6);

        Pagina<String> convertida = pagina.mapear(numero -> "n" + numero);

        assertThat(convertida.conteudo()).containsExactly("n1", "n2");
        assertThat(convertida.pagina()).isEqualTo(1);
        assertThat(convertida.totalElementos()).isEqualTo(6);
        assertThat(convertida.totalPaginas()).isEqualTo(3);
    }
}
