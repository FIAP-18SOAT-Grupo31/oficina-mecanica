package br.com.fiap.oficina_mecanica.compartilhado.infrastructure.persistence;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TextoLikeTest {

    @Test
    void montaPadraoContemEmMinusculas() {
        assertThat(TextoLike.contem("ArGo")).isEqualTo("%argo%");
    }

    @Test
    void tratamCuringasEBarraComoTextoLiteral() {
        assertThat(TextoLike.contem("100%_a\\b")).isEqualTo("%100\\%\\_a\\\\b%");
    }

    @Test
    void usaABarraInvertidaComoEscape() {
        assertThat(TextoLike.ESCAPE).isEqualTo('\\');
    }
}
