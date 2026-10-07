package br.com.fiap.oficina_mecanica.cliente.domain;

import java.util.UUID;

public interface ClienteRepository {
    boolean existePorId(UUID id);
}
