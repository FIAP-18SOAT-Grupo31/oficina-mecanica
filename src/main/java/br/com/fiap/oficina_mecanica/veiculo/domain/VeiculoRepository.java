package br.com.fiap.oficina_mecanica.veiculo.domain;

import java.util.UUID;

public interface VeiculoRepository {
    boolean existePorId(UUID id);
}
