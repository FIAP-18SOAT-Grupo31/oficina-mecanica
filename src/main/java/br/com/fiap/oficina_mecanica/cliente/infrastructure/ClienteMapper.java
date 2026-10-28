package br.com.fiap.oficina_mecanica.cliente.infrastructure;

import br.com.fiap.oficina_mecanica.cliente.domain.Cliente;
import br.com.fiap.oficina_mecanica.cliente.infrastructure.entity.ClienteEntity;

public class ClienteMapper {
    public static ClienteEntity toEntity (Cliente cliente){
        return new ClienteEntity(
                cliente.getId(),
                cliente.getNome(),
                cliente.getDocumento(),
                cliente.getEmail(),
                cliente.getTelefone(),
                EnderecoMapper.toEmbeddable(cliente.getEndereco())
        );
    }

    public static Cliente toDomain (ClienteEntity clienteEntity){
        return Cliente.reconstituir(
                clienteEntity.getId(),
                clienteEntity.getDocumento(),
                clienteEntity.getNome(),
                clienteEntity.getTelefone(),
                clienteEntity.getEmail(),
                EnderecoMapper.toDomain(clienteEntity.getEndereco())
        );
    }
}
