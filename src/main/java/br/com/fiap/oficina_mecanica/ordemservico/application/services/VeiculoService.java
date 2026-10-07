package br.com.fiap.oficina_mecanica.ordemservico.application.services;

import java.util.UUID;

public interface VeiculoService {

    public boolean veiculoExistePorId(UUID veiculoId);
}
