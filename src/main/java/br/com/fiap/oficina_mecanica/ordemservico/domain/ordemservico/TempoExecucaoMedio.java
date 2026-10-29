package br.com.fiap.oficina_mecanica.ordemservico.domain.ordemservico;

import java.time.Duration;

/** Tempo médio entre o início da execução (EM_EXECUCAO) e a finalização (FINALIZADA); média nula quando não há OS finalizada. */
public record TempoExecucaoMedio(Duration media, long totalOrdens) {
}
