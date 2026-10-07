package br.com.fiap.oficina_mecanica.autenticacao.domain;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UsuarioTest {

    @Test
    void criaUsuarioComIdentificador() {
        Usuario usuario = new Usuario("atendente", "hash", Papel.ATENDENTE);

        assertThat(usuario.getId()).isNotNull();
        assertThat(usuario.getLogin()).isEqualTo("atendente");
        assertThat(usuario.getSenhaHash()).isEqualTo("hash");
        assertThat(usuario.getPapel()).isEqualTo(Papel.ATENDENTE);
    }

    @Test
    void exigeLoginSenhaEPapel() {
        UUID id = UUID.randomUUID();

        assertThatThrownBy(() -> new Usuario(id, " ", "hash", Papel.ADMIN)).hasMessageContaining("login");
        assertThatThrownBy(() -> new Usuario(id, null, "hash", Papel.ADMIN)).hasMessageContaining("login");
        assertThatThrownBy(() -> new Usuario(id, "admin", "", Papel.ADMIN)).hasMessageContaining("senha");
        assertThatThrownBy(() -> new Usuario(id, "admin", null, Papel.ADMIN)).hasMessageContaining("senha");
        assertThatThrownBy(() -> new Usuario(id, "admin", "hash", null)).hasMessageContaining("papel");
    }
}
