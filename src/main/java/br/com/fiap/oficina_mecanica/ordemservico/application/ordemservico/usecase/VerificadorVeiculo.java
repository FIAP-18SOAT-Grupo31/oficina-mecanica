package br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.usecase;

import java.util.Optional;
import java.util.UUID;

public interface VerificadorVeiculo {
    Optional<VeiculoCadastrado> buscarPorId(UUID veiculoId);

    Optional<VeiculoCadastrado> buscarPorPlaca(String placa);

    record VeiculoCadastrado(UUID id, UUID clienteId) {}
}
