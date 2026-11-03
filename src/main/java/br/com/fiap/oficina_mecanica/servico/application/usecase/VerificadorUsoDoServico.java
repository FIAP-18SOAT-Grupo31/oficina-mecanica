package br.com.fiap.oficina_mecanica.servico.application.usecase;

import java.util.UUID;

public interface VerificadorUsoDoServico {

    boolean usadoEmOrcamento(UUID servicoId);
}
