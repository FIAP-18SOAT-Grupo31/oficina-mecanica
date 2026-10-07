package br.com.fiap.oficina_mecanica.compartilhado.infrastructure.web;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

class EscritorErroRespostaTest {

    private final EscritorErroResposta escritor = new EscritorErroResposta(JsonMapper.builder().build());

    @Test
    void escreveOErroRespostaEmJsonComOStatusDoTipo() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        escritor.escrever(response, TipoErro.MUITAS_REQUISICOES, "Muitas tentativas. Aguarde e tente novamente.");

        String corpo = response.getContentAsString();
        assertThat(response.getStatus()).isEqualTo(429);
        assertThat(response.getContentType()).startsWith("application/json");
        assertThat((Integer) JsonPath.read(corpo, "$.status")).isEqualTo(429);
        assertThat((String) JsonPath.read(corpo, "$.erro")).isEqualTo("Muitas Requisições");
        assertThat((String) JsonPath.read(corpo, "$.mensagem")).isEqualTo("Muitas tentativas. Aguarde e tente novamente.");
        assertThat((String) JsonPath.read(corpo, "$.timestamp")).isNotBlank();
    }
}
