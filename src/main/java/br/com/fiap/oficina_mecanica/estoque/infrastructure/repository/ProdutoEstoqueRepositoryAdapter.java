package br.com.fiap.oficina_mecanica.estoque.infrastructure.repository;

import br.com.fiap.oficina_mecanica.compartilhado.domain.Pagina;
import br.com.fiap.oficina_mecanica.compartilhado.infrastructure.persistence.TextoLike;
import br.com.fiap.oficina_mecanica.estoque.domain.ProdutoEstoque;
import br.com.fiap.oficina_mecanica.estoque.domain.ProdutoEstoqueRepository;
import br.com.fiap.oficina_mecanica.estoque.infrastructure.repository.entity.ProdutoEstoqueEntity;
import br.com.fiap.oficina_mecanica.estoque.infrastructure.repository.entity.ProdutoEstoqueMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class ProdutoEstoqueRepositoryAdapter implements ProdutoEstoqueRepository {

    private final ProdutoEstoqueJpaRepository jpaRepository;
    private final ProdutoEstoqueMapper mapper;

    public ProdutoEstoqueRepositoryAdapter(ProdutoEstoqueJpaRepository jpaRepository, ProdutoEstoqueMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public ProdutoEstoque salvar(ProdutoEstoque produto) {
        ProdutoEstoqueEntity entity = mapper.toEntity(produto);
        ProdutoEstoqueEntity salva = jpaRepository.save(entity);
        return mapper.toDomain(salva);
    }

    @Override
    public Optional<ProdutoEstoque> buscarPorId(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<ProdutoEstoque> buscarPorNomePeca(String nomePeca) {
        return jpaRepository.findByNomePeca(nomePeca).map(mapper::toDomain);
    }

    @Override
    public Pagina<ProdutoEstoque> listar(String nomePeca, int pagina, int tamanho) {
        PageRequest pageRequest = PageRequest.of(pagina, tamanho, Sort.by("nomePeca", "id"));
        Page<ProdutoEstoqueEntity> resultado = jpaRepository.findAll(filtroPorNome(nomePeca), pageRequest);

        List<ProdutoEstoque> produtos = resultado.getContent().stream().map(mapper::toDomain).toList();
        return new Pagina<>(produtos, pagina, tamanho, resultado.getTotalElements());
    }

    @Override
    public void excluirPorId(UUID id) {
        jpaRepository.deleteById(id);
    }

    private static Specification<ProdutoEstoqueEntity> filtroPorNome(String nomePeca) {
        return (root, query, builder) -> nomePeca == null
                ? builder.conjunction()
                : builder.like(builder.lower(root.<String>get("nomePeca")), TextoLike.contem(nomePeca), TextoLike.ESCAPE);
    }
}