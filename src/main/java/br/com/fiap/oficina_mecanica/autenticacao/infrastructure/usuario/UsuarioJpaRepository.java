package br.com.fiap.oficina_mecanica.autenticacao.infrastructure.usuario;

import br.com.fiap.oficina_mecanica.autenticacao.infrastructure.usuario.entity.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsuarioJpaRepository extends JpaRepository<UsuarioEntity, UUID> {

    Optional<UsuarioEntity> findByLogin(String login);

    boolean existsByLogin(String login);
}
