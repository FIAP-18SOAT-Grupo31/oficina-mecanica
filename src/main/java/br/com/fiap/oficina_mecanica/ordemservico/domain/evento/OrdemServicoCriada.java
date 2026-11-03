package br.com.fiap.oficina_mecanica.ordemservico.domain.evento;

import java.util.UUID;

public record OrdemServicoCriada(UUID ordemServicoId, String codigoAcesso) {}
