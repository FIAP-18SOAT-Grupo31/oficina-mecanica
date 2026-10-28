package br.com.fiap.oficina_mecanica.veiculo.infrastructure;

import br.com.fiap.oficina_mecanica.veiculo.domain.Veiculo;
import br.com.fiap.oficina_mecanica.veiculo.infrastructure.entity.VeiculoEntity;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VeiculoMapperTest {

    @Test
    void converteDominioParaEntityEDeVolta() {
        UUID clienteId = UUID.randomUUID();
        Veiculo veiculo = new Veiculo(2022, "abc1d23", "Argo", "Fiat", clienteId);

        VeiculoEntity entity = VeiculoMapper.toEntity(veiculo);
        Veiculo convertido = VeiculoMapper.toDomain(entity);

        assertThat(entity.getId()).isEqualTo(veiculo.getId());
        assertThat(entity.getClienteId()).isEqualTo(clienteId);
        assertThat(entity.getPlaca()).isEqualTo("ABC1D23");
        assertThat(entity.getMarca()).isEqualTo("Fiat");
        assertThat(entity.getModelo()).isEqualTo("Argo");
        assertThat(entity.getAno()).isEqualTo(2022);
        assertThat(convertido.getId()).isEqualTo(veiculo.getId());
        assertThat(convertido.getClienteId()).isEqualTo(clienteId);
        assertThat(convertido.getPlaca()).isEqualTo("ABC1D23");
        assertThat(convertido.getAnoFabricacao()).isEqualTo(2022);
    }

    @Test
    void rejeitaEntityComDadosInvalidosAoConverterParaDominio() {
        VeiculoEntity entity = new VeiculoEntity(UUID.randomUUID(), UUID.randomUUID(), "AB123", "Fiat", "Argo", 2022);

        assertThatThrownBy(() -> VeiculoMapper.toDomain(entity)).isInstanceOf(IllegalArgumentException.class);
    }
}
