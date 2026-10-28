package br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.usecase;

import java.util.Optional;
import java.util.UUID;

public interface VerificadorCliente {
    boolean existe(UUID clienteId);

    /** Busca o id do cliente pelo CPF/CNPJ já normalizado (só letras e dígitos, em maiúsculas). */
    Optional<UUID> buscarIdPorDocumento(String documento);
}
