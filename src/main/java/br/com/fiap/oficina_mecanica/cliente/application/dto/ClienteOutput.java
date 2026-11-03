package br.com.fiap.oficina_mecanica.cliente.application.dto;

import br.com.fiap.oficina_mecanica.cliente.domain.Cliente;

import java.util.UUID;

public record ClienteOutput(
        UUID id,
        String nome,
        String documento,
        String telefone,
        String email,
        EnderecoOutput endereco
) {

    public static ClienteOutput de(Cliente cliente) {
        return new ClienteOutput(
                cliente.getId(),
                cliente.getNome(),
                cliente.getDocumento(),
                cliente.getTelefone(),
                cliente.getEmail(),
                EnderecoOutput.de(cliente.getEndereco())
        );
    }
}
