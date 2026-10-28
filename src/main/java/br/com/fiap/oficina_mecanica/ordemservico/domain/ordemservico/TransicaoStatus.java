package br.com.fiap.oficina_mecanica.ordemservico.domain.ordemservico;

import java.time.LocalDateTime;
import java.util.UUID;

public record TransicaoStatus(
        UUID id,
        StatusOrdemServico statusAnterior,
        StatusOrdemServico statusNovo,
        LocalDateTime dataTransicao
) {
}
