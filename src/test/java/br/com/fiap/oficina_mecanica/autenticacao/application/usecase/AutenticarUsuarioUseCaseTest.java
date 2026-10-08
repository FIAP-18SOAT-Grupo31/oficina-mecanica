package br.com.fiap.oficina_mecanica.autenticacao.application.usecase;

import br.com.fiap.oficina_mecanica.autenticacao.application.dto.LoginInput;
import br.com.fiap.oficina_mecanica.autenticacao.application.dto.TokenOutput;
import br.com.fiap.oficina_mecanica.autenticacao.domain.Papel;
import br.com.fiap.oficina_mecanica.autenticacao.domain.Usuario;
import br.com.fiap.oficina_mecanica.autenticacao.domain.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AutenticarUsuarioUseCaseTest {

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(4);
    private final Map<String, Usuario> usuarios = new HashMap<>();
    private final UsuarioRepository repository = new UsuarioRepository() {
        @Override
        public Usuario salvar(Usuario usuario) {
            usuarios.put(usuario.getLogin(), usuario);
            return usuario;
        }

        @Override
        public Optional<Usuario> buscarPorLogin(String login) {
            return Optional.ofNullable(usuarios.get(login));
        }

        @Override
        public boolean existePorLogin(String login) {
            return usuarios.containsKey(login);
        }
    };
    private final GeradorToken geradorToken = usuario -> new TokenOutput("token-" + usuario.getLogin(), "Bearer", 3600);
    private final AutenticarUsuarioUseCase useCase = new AutenticarUsuarioUseCase(repository, passwordEncoder, geradorToken);

    @Test
    void geraTokenQuandoLoginESenhaConferem() {
        repository.salvar(new Usuario("atendente", passwordEncoder.encode("segredo"), Papel.ATENDENTE));

        TokenOutput token = useCase.execute(new LoginInput("atendente", "segredo"));

        assertThat(token.accessToken()).isEqualTo("token-atendente");
        assertThat(token.tokenType()).isEqualTo("Bearer");
    }

    @Test
    void recusaSenhaErrada() {
        repository.salvar(new Usuario("atendente", passwordEncoder.encode("segredo"), Papel.ATENDENTE));
        LoginInput input = new LoginInput("atendente", "errada");

        assertThatThrownBy(() -> useCase.execute(input)).isInstanceOf(CredenciaisInvalidasException.class);
    }

    @Test
    void recusaUsuarioInexistenteComAMesmaMensagem() {
        LoginInput input = new LoginInput("ninguem", "qualquer");

        assertThatThrownBy(() -> useCase.execute(input))
                .isInstanceOf(CredenciaisInvalidasException.class)
                .hasMessage("Usuário ou senha inválidos.");
    }
}
