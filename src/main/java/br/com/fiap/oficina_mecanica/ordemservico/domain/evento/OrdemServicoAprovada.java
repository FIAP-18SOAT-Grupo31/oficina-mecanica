package br.com.fiap.oficina_mecanica.ordemservico.domain.evento;

import java.util.UUID;

public record OrdemServicoAprovada(UUID ordemServicoId, String codigoAcesso, boolean aprovacaoParcial) {}
