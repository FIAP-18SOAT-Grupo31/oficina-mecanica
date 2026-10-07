package br.com.fiap.oficina_mecanica.compartilhado.infrastructure.web;

import java.time.LocalDateTime;

public record ErroResposta(
        int status,
        String erro,
        String mensagem,
        LocalDateTime timestamp
) {

    public static ErroResposta de(TipoErro tipo, String mensagem) {
        return new ErroResposta(tipo.status().value(), tipo.titulo(), mensagem, LocalDateTime.now());
    }
}
