package br.com.fiap.oficina_mecanica.veiculo.application.usecase;

import java.util.UUID;

public interface VerificadorOrdensServicoDoVeiculo {

    boolean possuiOrdensServico(UUID veiculoId);
}
