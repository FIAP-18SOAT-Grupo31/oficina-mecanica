package br.com.fiap.oficina_mecanica.veiculo.application.usecase;

import java.util.UUID;

public interface VerificadorDonoDoVeiculo {
    boolean existe(UUID clienteId);
}
