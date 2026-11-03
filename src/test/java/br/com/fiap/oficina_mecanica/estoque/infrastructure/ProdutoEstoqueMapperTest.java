package br.com.fiap.oficina_mecanica.estoque.infrastructure;

import br.com.fiap.oficina_mecanica.estoque.domain.ProdutoEstoque;
import br.com.fiap.oficina_mecanica.estoque.infrastructure.repository.entity.ProdutoEstoqueEntity;
import br.com.fiap.oficina_mecanica.estoque.infrastructure.repository.entity.ProdutoEstoqueMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ProdutoEstoqueMapperTest {

    private final ProdutoEstoqueMapper mapper = new ProdutoEstoqueMapper();

    @Test
    void converteDominioParaEntity() {
        ProdutoEstoque dominio = new ProdutoEstoque(UUID.randomUUID(), "Vela de Ignição", 20, new BigDecimal("45.00"));
        dominio.reservar(5);

        ProdutoEstoqueEntity entity = mapper.toEntity(dominio);

        assertThat(entity.getId()).isEqualTo(dominio.getId());
        assertThat(entity.getNomePeca()).isEqualTo("Vela de Ignição");
        assertThat(entity.getQuantidadeDisponivel()).isEqualTo(15);
        assertThat(entity.getQuantidadeReservada()).isEqualTo(5);
        assertThat(entity.getValorUnitario()).isEqualTo(new BigDecimal("45.00"));
    }

    @Test
    void converteEntityParaDominio() {
        ProdutoEstoqueEntity entity = new ProdutoEstoqueEntity(
                UUID.randomUUID(), "Pastilha de Freio", 8, 2, new BigDecimal("120.00")
        );

        ProdutoEstoque dominio = mapper.toDomain(entity);

        assertThat(dominio.getId()).isEqualTo(entity.getId());
        assertThat(dominio.getNomePeca()).isEqualTo("Pastilha de Freio");
        assertThat(dominio.getQuantidadeDisponivel()).isEqualTo(8);
        assertThat(dominio.getQuantidadeReservada()).isEqualTo(2);
        assertThat(dominio.getValorUnitario()).isEqualTo(new BigDecimal("120.00"));
    }
}