package br.com.fiap.oficina_mecanica.veiculo.infrastructure;

import br.com.fiap.oficina_mecanica.veiculo.domain.Veiculo;
import br.com.fiap.oficina_mecanica.veiculo.infrastructure.entity.VeiculoEntity;

public class VeiculoMapper {

    public static VeiculoEntity toEntity(Veiculo veiculo) {
        return new VeiculoEntity(
                veiculo.getId(),
                veiculo.getClienteId(),
                veiculo.getPlaca(),
                veiculo.getMarca(),
                veiculo.getModelo(),
                veiculo.getAnoFabricacao()
        );
    }

    public static Veiculo toDomain(VeiculoEntity entity) {
        return Veiculo.reconstituir(
                entity.getId(),
                entity.getAno(),
                entity.getPlaca(),
                entity.getModelo(),
                entity.getMarca(),
                entity.getClienteId()
        );
    }
}
