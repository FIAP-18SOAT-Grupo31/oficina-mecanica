package br.com.fiap.oficina_mecanica.estoque.infrastructure.repository;

import br.com.fiap.oficina_mecanica.estoque.domain.ProdutoEstoque;
import br.com.fiap.oficina_mecanica.estoque.domain.ProdutoEstoqueRepository;
import br.com.fiap.oficina_mecanica.estoque.infrastructure.repository.entity.ProdutoEstoqueMapper;
import br.com.fiap.oficina_mecanica.estoque.infrastructure.repository.entity.ProdutoEstoqueEntity;
import org.springframework.stereotype.Component;

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
}