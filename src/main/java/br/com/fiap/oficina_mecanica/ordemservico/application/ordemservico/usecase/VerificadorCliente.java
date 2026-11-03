package br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.usecase;

import java.util.Optional;
import java.util.UUID;

public interface VerificadorCliente {
    boolean existe(UUID clienteId);

    Optional<UUID> buscarIdPorDocumento(String documento);
}
