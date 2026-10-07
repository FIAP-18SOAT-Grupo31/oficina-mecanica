package br.com.fiap.oficina_mecanica.compartilhado.infrastructure.config;

import br.com.fiap.oficina_mecanica.compartilhado.infrastructure.web.EscritorErroResposta;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import tools.jackson.databind.json.JsonMapper;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class LimiteRequisicoesFilterTest {

    private static final class RelogioAjustavel extends Clock {

        private Instant agora = Instant.parse("2026-01-01T10:00:00Z");

        void avancar(Duration duracao) {
            agora = agora.plus(duracao);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return agora;
        }
    }

    private final RelogioAjustavel relogio = new RelogioAjustavel();
    private final EscritorErroResposta escritor = new EscritorErroResposta(JsonMapper.builder().build());

    private int chamar(LimiteRequisicoesFilter filtro, String uri, String ip) throws Exception {
        return executar(filtro, uri, ip).getStatus();
    }

    private MockHttpServletResponse executar(LimiteRequisicoesFilter filtro, String uri, String ip) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", uri);
        request.setRemoteAddr(ip);
        MockHttpServletResponse response = new MockHttpServletResponse();
        filtro.doFilter(request, response, new MockFilterChain());
        return response;
    }

    @Test
    void bloqueiaDepoisDoLimiteSeparandoPorIpERota() throws Exception {
        LimiteRequisicoesFilter filtro = new LimiteRequisicoesFilter(2, relogio, escritor);

        assertThat(chamar(filtro, "/api/auth/login", "1.1.1.1")).isEqualTo(200);
        assertThat(chamar(filtro, "/api/auth/login", "1.1.1.1")).isEqualTo(200);
        assertThat(chamar(filtro, "/api/auth/login", "1.1.1.1")).isEqualTo(429);
        assertThat(chamar(filtro, "/api/auth/login", "2.2.2.2")).isEqualTo(200);
        assertThat(chamar(filtro, "/api/publico/ordens-servico/ABC", "1.1.1.1")).isEqualTo(200);
    }

    @Test
    void liberaNovamenteNaProximaJanela() throws Exception {
        LimiteRequisicoesFilter filtro = new LimiteRequisicoesFilter(1, relogio, escritor);

        assertThat(chamar(filtro, "/api/auth/login", "1.1.1.1")).isEqualTo(200);
        assertThat(chamar(filtro, "/api/auth/login", "1.1.1.1")).isEqualTo(429);
        relogio.avancar(Duration.ofSeconds(61));
        assertThat(chamar(filtro, "/api/auth/login", "1.1.1.1")).isEqualTo(200);
    }

    @Test
    void bloqueioVoltaErroRespostaComRetryAfter() throws Exception {
        LimiteRequisicoesFilter filtro = new LimiteRequisicoesFilter(1, relogio, escritor);
        executar(filtro, "/api/auth/login", "1.1.1.1");

        MockHttpServletResponse response = executar(filtro, "/api/auth/login", "1.1.1.1");

        assertThat(response.getStatus()).isEqualTo(429);
        assertThat(response.getHeader("Retry-After")).isEqualTo("61");
        assertThat((String) JsonPath.read(response.getContentAsString(), "$.erro")).isEqualTo("Muitas Requisições");
    }

    @Test
    void naoLimitaRotasAutenticadasPorJwt() throws Exception {
        LimiteRequisicoesFilter filtro = new LimiteRequisicoesFilter(0, relogio, escritor);

        assertThat(chamar(filtro, "/api/orcamentos", "1.1.1.1")).isEqualTo(200);
    }
}
