package br.com.fiap.oficina_mecanica.estoque.infrastructure;

import br.com.fiap.oficina_mecanica.estoque.domain.ProdutoEstoque;
import br.com.fiap.oficina_mecanica.estoque.infrastructure.repository.ProdutoEstoqueJpaRepository;
import br.com.fiap.oficina_mecanica.estoque.infrastructure.repository.ProdutoEstoqueRepositoryAdapter;
import br.com.fiap.oficina_mecanica.estoque.infrastructure.repository.entity.ProdutoEstoqueEntity;
import br.com.fiap.oficina_mecanica.estoque.infrastructure.repository.entity.ProdutoEstoqueMapper;
import org.junit.jupiter.api.DisplayName;
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
import static org.mockito.Mockito.verifyNoInteractions;

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
    @DisplayName("Deve converter domínio para entity, salvar no JPA e retornar domínio salvo")
    void deveSalvarProduto() {
        ProdutoEstoque dominioInput = new ProdutoEstoque(UUID.randomUUID(), "PNEU", 4, new BigDecimal("350.00"));
        ProdutoEstoqueEntity entityToSave = new ProdutoEstoqueEntity(dominioInput.getId(), "PNEU", 4, 0, new BigDecimal("350.00"));
        ProdutoEstoqueEntity entitySaved = new ProdutoEstoqueEntity(dominioInput.getId(), "PNEU", 4, 0, new BigDecimal("350.00"));
        ProdutoEstoque dominioOutput = new ProdutoEstoque(dominioInput.getId(), "PNEU", 4, new BigDecimal("350.00"));

        when(mapper.toEntity(dominioInput)).thenReturn(entityToSave);
        when(jpaRepository.save(entityToSave)).thenReturn(entitySaved);
        when(mapper.toDomain(entitySaved)).thenReturn(dominioOutput);

        ProdutoEstoque salvo = adapter.salvar(dominioInput);

        assertThat(salvo).isNotNull();
        assertThat(salvo.getId()).isEqualTo(dominioInput.getId());
        assertThat(salvo.getNomePeca()).isEqualTo("PNEU");

        verify(mapper).toEntity(dominioInput);
        verify(jpaRepository).save(entityToSave);
        verify(mapper).toDomain(entitySaved);
    }

    @Test
    @DisplayName("Deve buscar produto por ID quando o registro existir")
    void deveBuscarProdutoPorIdQuandoExistir() {
        UUID id = UUID.randomUUID();
        ProdutoEstoqueEntity entity = new ProdutoEstoqueEntity(id, "PNEU", 4, 0, new BigDecimal("350.00"));
        ProdutoEstoque dominio = new ProdutoEstoque(id, "PNEU", 4, new BigDecimal("350.00"));

        when(jpaRepository.findById(id)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(dominio);

        Optional<ProdutoEstoque> encontrado = adapter.buscarPorId(id);

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getId()).isEqualTo(id);
        assertThat(encontrado.get().getNomePeca()).isEqualTo("PNEU");

        verify(jpaRepository).findById(id);
        verify(mapper).toDomain(entity);
    }

    @Test
    @DisplayName("Deve retornar Optional vazio ao buscar por ID inexistente")
    void deveRetornarVazioQuandoNaoEncontrarProdutoPorId() {
        UUID id = UUID.randomUUID();
        when(jpaRepository.findById(id)).thenReturn(Optional.empty());

        Optional<ProdutoEstoque> encontrado = adapter.buscarPorId(id);

        assertThat(encontrado).isEmpty();
        verify(jpaRepository).findById(id);
        verifyNoInteractions(mapper);
    }

    @Test
    @DisplayName("Deve buscar produto por nome da peça quando o registro existir")
    void deveBuscarProdutoPorNomePecaQuandoExistir() {
        String nomePeca = "FILTRO DE ÓLEO";
        UUID id = UUID.randomUUID();
        ProdutoEstoqueEntity entity = new ProdutoEstoqueEntity(id, nomePeca, 10, 0, new BigDecimal("45.00"));
        ProdutoEstoque dominio = new ProdutoEstoque(id, nomePeca, 10, new BigDecimal("45.00"));

        when(jpaRepository.findByNomePeca(nomePeca)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(dominio);

        Optional<ProdutoEstoque> encontrado = adapter.buscarPorNomePeca(nomePeca);

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getNomePeca()).isEqualTo(nomePeca);

        verify(jpaRepository).findByNomePeca(nomePeca);
        verify(mapper).toDomain(entity);
    }

    @Test
    @DisplayName("Deve retornar Optional vazio ao buscar por nome da peça inexistente")
    void deveRetornarVazioQuandoNaoEncontrarProdutoPorNomePeca() {
        String nomePeca = "PEÇA INEXISTENTE";
        when(jpaRepository.findByNomePeca(nomePeca)).thenReturn(Optional.empty());

        Optional<ProdutoEstoque> encontrado = adapter.buscarPorNomePeca(nomePeca);

        assertThat(encontrado).isEmpty();
        verify(jpaRepository).findByNomePeca(nomePeca);
        verifyNoInteractions(mapper);
    }
}