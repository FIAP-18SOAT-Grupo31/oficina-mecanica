package br.com.fiap.oficina_mecanica.compartilhado.infrastructure.web;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class EscritorErroResposta {

    private final JsonMapper jsonMapper;

    public EscritorErroResposta(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    public void escrever(HttpServletResponse response, TipoErro tipo, String mensagem) throws IOException {
        response.setStatus(tipo.status().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        jsonMapper.writeValue(response.getWriter(), ErroResposta.de(tipo, mensagem));
    }
}
