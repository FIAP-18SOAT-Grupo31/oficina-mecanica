package br.com.fiap.oficina_mecanica.compartilhado.domain;

import java.time.LocalDateTime;
import java.time.ZoneId;

public final class Horario {

    public static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");

    private Horario() {
    }

    public static LocalDateTime agora() {
        return LocalDateTime.now(FUSO);
    }
}
