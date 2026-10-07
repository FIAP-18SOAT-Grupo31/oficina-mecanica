package br.com.fiap.oficina_mecanica.autenticacao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ErrosComServidorRealIntegrationTest {

    private final HttpClient cliente = HttpClient.newHttpClient();

    @Value("${local.server.port}")
    private int porta;

    @Test
    void loginInvalidoRetorna400EmVezDe401() throws Exception {
        var resposta = postarLogin("{\"login\":\"\"}");

        assertThat(resposta.statusCode()).isEqualTo(400);
    }

    @Test
    void senhaErradaContinuaRetornando401() throws Exception {
        var resposta = postarLogin("{\"login\":\"admin\",\"senha\":\"errada\"}");

        assertThat(resposta.statusCode()).isEqualTo(401);
    }

    private HttpResponse<String> postarLogin(String corpo) throws Exception {
        var requisicao = HttpRequest.newBuilder(URI.create("http://localhost:" + porta + "/api/auth/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(corpo))
                .build();
        return cliente.send(requisicao, HttpResponse.BodyHandlers.ofString());
    }
}
