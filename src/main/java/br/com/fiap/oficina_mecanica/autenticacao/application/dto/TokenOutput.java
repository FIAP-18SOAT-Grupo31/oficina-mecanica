package br.com.fiap.oficina_mecanica.autenticacao.application.dto;

public record TokenOutput(
        String accessToken,
        String tokenType,
        long expiresIn
) {}
