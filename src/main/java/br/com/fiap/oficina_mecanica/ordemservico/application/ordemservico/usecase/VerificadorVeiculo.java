package br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.usecase;

import java.util.UUID;

public interface VerificadorVeiculo {
    boolean existe(UUID veiculoId);
}
