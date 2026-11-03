package br.com.fiap.oficina_mecanica.servico.infrastructure;

import br.com.fiap.oficina_mecanica.servico.domain.Servico;
import br.com.fiap.oficina_mecanica.servico.infrastructure.entity.ServicoEntity;

public final class ServicoMapper {

    private ServicoMapper() {
    }

    public static ServicoEntity toEntity(Servico servico) {
        return new ServicoEntity(
                servico.getId(),
                servico.getNome(),
                servico.getDescricao(),
                servico.getValorMaoDeObra()
        );
    }

    public static Servico toDomain(ServicoEntity entity) {
        return Servico.reconstituir(
                entity.getId(),
                entity.getNome(),
                entity.getDescricao(),
                entity.getValorMaoDeObra()
        );
    }
}
