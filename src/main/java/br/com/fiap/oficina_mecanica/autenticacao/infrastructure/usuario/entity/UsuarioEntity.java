package br.com.fiap.oficina_mecanica.autenticacao.infrastructure.usuario.entity;

import br.com.fiap.oficina_mecanica.autenticacao.domain.Papel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "usuarios")
public class UsuarioEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 60)
    private String login;

    @Column(name = "senha_hash", nullable = false, length = 100)
    private String senhaHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Papel papel;

    protected UsuarioEntity() {}

    public UsuarioEntity(UUID id, String login, String senhaHash, Papel papel) {
        this.id = id;
        this.login = login;
        this.senhaHash = senhaHash;
        this.papel = papel;
    }

    public UUID getId() { return id; }
    public String getLogin() { return login; }
    public String getSenhaHash() { return senhaHash; }
    public Papel getPapel() { return papel; }
}
