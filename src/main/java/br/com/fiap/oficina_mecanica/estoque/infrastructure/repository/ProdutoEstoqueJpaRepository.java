package br.com.fiap.oficina_mecanica.estoque.infrastructure.repository;

import br.com.fiap.oficina_mecanica.estoque.infrastructure.repository.entity.ProdutoEstoqueEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProdutoEstoqueJpaRepository extends JpaRepository<ProdutoEstoqueEntity, UUID> {

    Optional<ProdutoEstoqueEntity> findByNomePeca(String nomePeca);
}