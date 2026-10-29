package br.com.fiap.oficina_mecanica.cliente.infrastructure;

import br.com.fiap.oficina_mecanica.cliente.domain.Cliente;
import br.com.fiap.oficina_mecanica.cliente.domain.ClienteRepository;
import br.com.fiap.oficina_mecanica.cliente.infrastructure.entity.ClienteEntity;
import br.com.fiap.oficina_mecanica.compartilhado.domain.Pagina;
import br.com.fiap.oficina_mecanica.compartilhado.infrastructure.persistence.TextoLike;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ClienteRepositoryAdapter implements ClienteRepository {

    private final ClienteJpaRepository jpaRepository;

    public ClienteRepositoryAdapter(ClienteJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Cliente salvar(Cliente cliente) {
        return ClienteMapper.toDomain(jpaRepository.save(ClienteMapper.toEntity(cliente)));
    }

    @Override
    public Optional<Cliente> buscarPorId(UUID id) {
        return jpaRepository.findById(id).map(ClienteMapper::toDomain);
    }

    @Override
    public Pagina<Cliente> listar(String nome, String documento, int pagina, int tamanho) {
        PageRequest pageRequest = PageRequest.of(pagina, tamanho, Sort.by("nome", "id"));
        Page<ClienteEntity> resultado = jpaRepository.findAll(filtro(nome, documento), pageRequest);

        List<Cliente> clientes = resultado.getContent().stream().map(ClienteMapper::toDomain).toList();
        return new Pagina<>(clientes, pagina, tamanho, resultado.getTotalElements());
    }

    @Override
    public boolean existePorId(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public boolean existePorDocumento(String documento) {
        return jpaRepository.existsByDocumento(documento);
    }

    @Override
    public boolean existeOutroComDocumento(String documento, UUID idIgnorado) {
        return jpaRepository.existsByDocumentoAndIdNot(documento, idIgnorado);
    }

    @Override
    public void excluirPorId(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public Optional<String> buscarDocumentoPorId(UUID id) {
        return jpaRepository.findById(id).map(ClienteEntity::getDocumento);
    }

    private static Specification<ClienteEntity> filtro(String nome, String documento) {
        return (root, query, builder) -> {
            List<Predicate> predicados = new ArrayList<>();

            if (nome != null) {
                predicados.add(builder.like(builder.lower(root.<String>get("nome")), TextoLike.contem(nome),
                        TextoLike.ESCAPE));
            }

            if (documento != null) {
                predicados.add(builder.equal(root.get("documento"), documento));
            }

            return builder.and(predicados.toArray(new Predicate[0]));
        };
    }
}
