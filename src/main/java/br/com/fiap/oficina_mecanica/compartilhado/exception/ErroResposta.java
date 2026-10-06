package br.com.fiap.oficina_mecanica.compartilhado.exception;

import java.time.LocalDateTime;

public record ErroResposta(
        int status,
        String erro,
        String mensagem,
        LocalDateTime timestamp
) {}