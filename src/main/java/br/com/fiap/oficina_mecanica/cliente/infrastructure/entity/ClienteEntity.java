package br.com.fiap.oficina_mecanica.cliente.infrastructure.entity;

import br.com.fiap.oficina_mecanica.cliente.infrastructure.EnderecoEmbeddable;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "clientes")
public class ClienteEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false, unique = true, length = 14)
    private String documento;

    @Column(length = 150)
    private String email;

    @Column(length = 20)
    private String telefone;

    @Embedded
    private EnderecoEmbeddable endereco;

    protected ClienteEntity() {}

    public ClienteEntity(UUID id, String nome, String documento, String email, String telefone, EnderecoEmbeddable enderecoEmbeddable) {
        this.id = id;
        this.nome = nome;
        this.documento = documento;
        this.email = email;
        this.telefone = telefone;
        this.endereco = enderecoEmbeddable;
    }

    public UUID getId() { return id; }
    public String getNome() { return nome; }
    public String getDocumento() { return documento; }
    public String getEmail() { return email; }
    public String getTelefone() { return telefone; }
    public EnderecoEmbeddable getEndereco() {return  endereco;}
}
