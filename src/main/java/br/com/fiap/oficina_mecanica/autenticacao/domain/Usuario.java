package br.com.fiap.oficina_mecanica.autenticacao.domain;

import java.util.UUID;

public class Usuario {
    private UUID id;
    private String login;
    private String senhaHash;
    private Papel papel;

    public Usuario(String login, String senhaHash, Papel papel) {
        this(UUID.randomUUID(), login, senhaHash, papel);
    }

    public Usuario(UUID id, String login, String senhaHash, Papel papel) {
        if (login == null || login.isBlank()) {
            throw new IllegalStateException("O login do usuário é obrigatório.");
        }
        if (senhaHash == null || senhaHash.isBlank()) {
            throw new IllegalStateException("A senha do usuário é obrigatória.");
        }
        if (papel == null) {
            throw new IllegalStateException("O papel do usuário é obrigatório.");
        }
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
