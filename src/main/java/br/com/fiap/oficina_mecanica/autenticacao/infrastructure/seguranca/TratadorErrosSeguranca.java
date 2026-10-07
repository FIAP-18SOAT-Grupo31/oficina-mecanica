package br.com.fiap.oficina_mecanica.autenticacao.infrastructure.seguranca;

import br.com.fiap.oficina_mecanica.compartilhado.infrastructure.web.EscritorErroResposta;
import br.com.fiap.oficina_mecanica.compartilhado.infrastructure.web.TipoErro;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class TratadorErrosSeguranca implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final EscritorErroResposta escritor;

    public TratadorErrosSeguranca(EscritorErroResposta escritor) {
        this.escritor = escritor;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException ex) throws IOException {
        response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
        escritor.escrever(response, TipoErro.NAO_AUTENTICADO, "Informe um token válido no cabeçalho Authorization.");
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException ex) throws IOException {
        escritor.escrever(response, TipoErro.ACESSO_NEGADO, "Seu perfil não tem permissão para este recurso.");
    }
}
