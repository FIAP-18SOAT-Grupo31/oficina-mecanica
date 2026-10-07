package br.com.fiap.oficina_mecanica.autenticacao.application.usecase;

import br.com.fiap.oficina_mecanica.autenticacao.application.dto.LoginInput;
import br.com.fiap.oficina_mecanica.autenticacao.application.dto.TokenOutput;
import br.com.fiap.oficina_mecanica.autenticacao.domain.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AutenticarUsuarioUseCase {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final GeradorToken geradorToken;

    public AutenticarUsuarioUseCase(UsuarioRepository repository, PasswordEncoder passwordEncoder, GeradorToken geradorToken) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.geradorToken = geradorToken;
    }

    public TokenOutput execute(LoginInput input) {
        return repository.buscarPorLogin(input.login())
                .filter(usuario -> passwordEncoder.matches(input.senha(), usuario.getSenhaHash()))
                .map(geradorToken::gerar)
                .orElseThrow(CredenciaisInvalidasException::new);
    }
}
