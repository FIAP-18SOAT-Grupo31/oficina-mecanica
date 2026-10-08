package br.com.fiap.oficina_mecanica.autenticacao.infrastructure.seguranca;

import br.com.fiap.oficina_mecanica.autenticacao.domain.Papel;
import br.com.fiap.oficina_mecanica.autenticacao.domain.Usuario;
import br.com.fiap.oficina_mecanica.autenticacao.domain.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class UsuariosDemonstracao implements ApplicationRunner {

    private static final Logger LOG = LoggerFactory.getLogger(UsuariosDemonstracao.class);
    private static final Map<String, Papel> USUARIOS = Map.of(
            "admin", Papel.ADMIN,
            "atendente", Papel.ATENDENTE);

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final String senha;

    public UsuariosDemonstracao(UsuarioRepository repository, PasswordEncoder passwordEncoder,
                                @Value("${app.seguranca.usuarios-demo.senha:}") String senha) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.senha = senha;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (senha.isBlank()) {
            LOG.warn("DEMO_PASSWORD não definida: os usuários de demonstração não foram criados.");
            return;
        }
        USUARIOS.forEach((login, papel) -> {
            if (!repository.existePorLogin(login)) {
                repository.salvar(new Usuario(login, passwordEncoder.encode(senha), papel));
                LOG.info("Usuário de demonstração criado: {} ({}).", login, papel);
            }
        });
    }
}
