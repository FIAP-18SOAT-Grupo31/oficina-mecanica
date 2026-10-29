package br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.dto;

import br.com.fiap.oficina_mecanica.ordemservico.domain.ordemservico.TempoExecucaoMedio;

public record TempoExecucaoMedioOutput(
        Long tempoMedioSegundos,
        String tempoMedio,
        long totalOrdensFinalizadas
) {

    public static TempoExecucaoMedioOutput de(TempoExecucaoMedio metrica) {
        if (metrica.media() == null) {
            return new TempoExecucaoMedioOutput(null, null, metrica.totalOrdens());
        }
        return new TempoExecucaoMedioOutput(
                metrica.media().getSeconds(), metrica.media().toString(), metrica.totalOrdens());
    }
}
