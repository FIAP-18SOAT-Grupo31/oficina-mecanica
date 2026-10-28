package br.com.fiap.oficina_mecanica.servico.infrastructure;

import br.com.fiap.oficina_mecanica.compartilhado.domain.Pagina;
import br.com.fiap.oficina_mecanica.compartilhado.infrastructure.persistence.TextoLike;
import br.com.fiap.oficina_mecanica.servico.domain.Servico;
import br.com.fiap.oficina_mecanica.servico.domain.ServicoRepository;
import br.com.fiap.oficina_mecanica.servico.infrastructure.entity.ServicoEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ServicoRepositoryAdapter implements ServicoRepository {

    private final ServicoJpaRepository jpaRepository;

    public ServicoRepositoryAdapter(ServicoJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Servico salvar(Servico servico) {
        return ServicoMapper.toDomain(jpaRepository.save(ServicoMapper.toEntity(servico)));
    }

    @Override
    public Optional<Servico> buscarPorId(UUID id) {
        return jpaRepository.findById(id).map(ServicoMapper::toDomain);
    }

    @Override
    public Pagina<Servico> listar(String nome, int pagina, int tamanho) {
        PageRequest pageRequest = PageRequest.of(pagina, tamanho, Sort.by("nome", "id"));
        Page<ServicoEntity> resultado = jpaRepository.findAll(filtro(nome), pageRequest);

        List<Servico> servicos = resultado.getContent().stream().map(ServicoMapper::toDomain).toList();
        return new Pagina<>(servicos, pagina, tamanho, resultado.getTotalElements());
    }

    @Override
    public boolean existePorId(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public boolean existePorNome(String nome) {
        return jpaRepository.existsByNomeIgnoreCase(nome);
    }

    @Override
    public boolean existeOutroComNome(String nome, UUID idIgnorado) {
        return jpaRepository.existsByNomeIgnoreCaseAndIdNot(nome, idIgnorado);
    }

    @Override
    public void excluirPorId(UUID id) {
        jpaRepository.deleteById(id);
    }

    private static Specification<ServicoEntity> filtro(String nome) {
        return (root, query, builder) -> nome == null
                ? builder.conjunction()
                : builder.like(builder.lower(root.<String>get("nome")), TextoLike.contem(nome), TextoLike.ESCAPE);
    }
}
