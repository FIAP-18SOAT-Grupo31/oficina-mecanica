package br.com.fiap.oficina_mecanica.autenticacao.infrastructure.usuario;

import br.com.fiap.oficina_mecanica.autenticacao.domain.Usuario;
import br.com.fiap.oficina_mecanica.autenticacao.infrastructure.usuario.entity.UsuarioEntity;
import org.springframework.stereotype.Component;

@Component
public class UsuarioMapper {

    public UsuarioEntity toEntity(Usuario dominio) {
        return new UsuarioEntity(dominio.getId(), dominio.getLogin(), dominio.getSenhaHash(), dominio.getPapel());
    }

    public Usuario toDomain(UsuarioEntity entity) {
        return new Usuario(entity.getId(), entity.getLogin(), entity.getSenhaHash(), entity.getPapel());
    }
}
