package br.com.fiap.oficina_mecanica.ordemservico.application.services;

import java.util.UUID;

public interface ClienteService {

    public boolean clienteExistePorId(UUID clienteId);
}
