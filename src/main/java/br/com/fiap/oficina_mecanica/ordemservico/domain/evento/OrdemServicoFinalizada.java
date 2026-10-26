package br.com.fiap.oficina_mecanica.ordemservico.domain.evento;

import java.util.UUID;

public record OrdemServicoFinalizada(UUID ordemServicoId, UUID veiculoId) {}
