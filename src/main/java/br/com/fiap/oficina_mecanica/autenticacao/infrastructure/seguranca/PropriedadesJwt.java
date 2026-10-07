package br.com.fiap.oficina_mecanica.autenticacao.infrastructure.seguranca;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

@ConfigurationProperties(prefix = "app.seguranca.jwt")
public record PropriedadesJwt(String segredo, Duration validade) {

    public PropriedadesJwt {
        if (segredo == null || segredo.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("JWT_SECRET precisa ter pelo menos 32 caracteres.");
        }
        if (validade == null) {
            validade = Duration.ofHours(1);
        }
    }
}
