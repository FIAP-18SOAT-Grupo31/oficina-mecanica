package br.com.fiap.oficina_mecanica.estoque.infrastructure;

import br.com.fiap.oficina_mecanica.estoque.domain.ProdutoEstoque;
import br.com.fiap.oficina_mecanica.estoque.infrastructure.entity.ProdutoEstoqueEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProdutoEstoqueRepositoryAdapterTest {

    @Mock
    private ProdutoEstoqueJpaRepository jpaRepository;

    @Mock
    private ProdutoEstoqueMapper mapper;

    @InjectMocks
    private ProdutoEstoqueRepositoryAdapter adapter;

    @Test
    void deveSalvarProduto() {
        ProdutoEstoque dominio = new ProdutoEstoque(UUID.randomUUID(), "Pneu", 4, new BigDecimal("350.00"));
        ProdutoEstoqueEntity entity = new ProdutoEstoqueEntity(dominio.getId(), "Pneu", 4, 0, new BigDecimal("350.00"));

        when(mapper.toEntity(dominio)).thenReturn(entity);
        when(jpaRepository.save(entity)).thenReturn(entity);
        when(mapper.toDomain(entity)).thenReturn(dominio);

        ProdutoEstoque salvo = adapter.salvar(dominio);

        assertThat(salvo).isNotNull();
        verify(jpaRepository).save(entity);
    }

    @Test
    void deveBuscarProdutoPorIdQuandoExistir() {
        UUID id = UUID.randomUUID();
        ProdutoEstoqueEntity entity = new ProdutoEstoqueEntity(id, "Pneu", 4, 0, new BigDecimal("350.00"));
        ProdutoEstoque dominio = new ProdutoEstoque(id, "Pneu", 4, new BigDecimal("350.00"));

        when(jpaRepository.findById(id)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(dominio);

        Optional<ProdutoEstoque> encontrado = adapter.buscarPorId(id);

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getNomePeca()).isEqualTo("Pneu");
    }

    @Test
    void deveRetornarVazioQuandoNaoEncontrarProduto() {
        UUID id = UUID.randomUUID();
        when(jpaRepository.findById(id)).thenReturn(Optional.empty());

        Optional<ProdutoEstoque> encontrado = adapter.buscarPorId(id);

        assertThat(encontrado).isEmpty();
    }
}