package br.com.fiap.oficina_mecanica.estoque.infrastructure;

import br.com.fiap.oficina_mecanica.estoque.infrastructure.entity.ProdutoEstoqueEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProdutoEstoqueJpaRepository extends JpaRepository<ProdutoEstoqueEntity, UUID> {
}