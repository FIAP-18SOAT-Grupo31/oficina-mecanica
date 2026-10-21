package br.com.fiap.oficina_mecanica.cliente.infrastructure;

import br.com.fiap.oficina_mecanica.cliente.domain.Estado;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Embeddable
public class EnderecoEmbeddable {

    private String rua;
    private String numero;
    private String complemento;
    private String bairro;
    private String cidade;

    @Enumerated(EnumType.STRING)
    private Estado estado;
    private String cep;


    public EnderecoEmbeddable(String rua, String numero, String complemento, String bairro, String cidade, Estado estado, String cep) {
        this.rua = rua;
        this.numero = numero;
        this.complemento = complemento;
        this.bairro = bairro;
        this.cidade = cidade;
        this.estado = estado;
        this.cep = cep;
    }

    protected EnderecoEmbeddable() {}

    public String getCep() {
        return cep;
    }

    public String getRua() {
        return rua;
    }

    public String getNumero() {
        return numero;
    }

    public String getComplemento() {
        return complemento;
    }

    public String getBairro() {
        return bairro;
    }

    public String getCidade() {
        return cidade;
    }

    public Estado getEstado() {
        return estado;
    }
}
