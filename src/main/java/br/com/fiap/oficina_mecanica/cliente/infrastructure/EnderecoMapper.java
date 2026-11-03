package br.com.fiap.oficina_mecanica.cliente.infrastructure;

import br.com.fiap.oficina_mecanica.cliente.domain.Endereco;

public class EnderecoMapper {

    public static EnderecoEmbeddable toEmbeddable(Endereco endereco) {
        if(endereco == null){
            throw new IllegalArgumentException("O endereco não pode ser null");
        }
        return new EnderecoEmbeddable(
                endereco.getRua(),
                endereco.getNumero(),
                endereco.getComplemento(),
                endereco.getBairro(),
                endereco.getCidade(),
                endereco.getEstado(),
                endereco.getCep()
        );
    }

    public static Endereco toDomain( EnderecoEmbeddable enderecoEmbeddable){
        if(enderecoEmbeddable == null){
            throw new IllegalArgumentException("O enderecoEmbeddable não pode ser null");
        }
        return new Endereco(
                enderecoEmbeddable.getRua(),
                enderecoEmbeddable.getNumero(),
                enderecoEmbeddable.getComplemento(),
                enderecoEmbeddable.getBairro(),
                enderecoEmbeddable.getCidade(),
                enderecoEmbeddable.getEstado(),
                enderecoEmbeddable.getCep()
        );
    }
}
