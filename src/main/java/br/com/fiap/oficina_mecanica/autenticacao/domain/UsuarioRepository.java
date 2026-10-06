package br.com.fiap.oficina_mecanica.autenticacao.domain;

import java.util.Optional;

public interface UsuarioRepository {
    Usuario salvar(Usuario usuario);
    Optional<Usuario> buscarPorLogin(String login);
    boolean existePorLogin(String login);
}
