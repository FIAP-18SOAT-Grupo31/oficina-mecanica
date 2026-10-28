package br.com.fiap.oficina_mecanica.cliente.application.dto;

import br.com.fiap.oficina_mecanica.cliente.domain.Endereco;
import br.com.fiap.oficina_mecanica.cliente.domain.Estado;

public record EnderecoOutput(
        String rua,
        String numero,
        String complemento,
        String bairro,
        String cidade,
        Estado estado,
        String cep
) {

    public static EnderecoOutput de(Endereco endereco) {
        return new EnderecoOutput(
                endereco.getRua(),
                endereco.getNumero(),
                endereco.getComplemento(),
                endereco.getBairro(),
                endereco.getCidade(),
                endereco.getEstado(),
                endereco.getCep()
        );
    }
}
