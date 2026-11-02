package br.com.fiap.oficina_mecanica.compartilhado.infrastructure.config;

import br.com.fiap.oficina_mecanica.compartilhado.infrastructure.web.EscritorErroResposta;
import br.com.fiap.oficina_mecanica.compartilhado.infrastructure.web.TipoErro;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Clock;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class LimiteRequisicoesFilter extends OncePerRequestFilter {

    private static final long JANELA_MS = 60_000;
    private static final int MAXIMO_CHAVES = 10_000;

    private record Janela(long inicio, AtomicInteger contador) {}

    private final int limitePorMinuto;
    private final Clock relogio;
    private final EscritorErroResposta escritor;
    private final Map<String, Janela> janelas = new ConcurrentHashMap<>();

    @Autowired
    public LimiteRequisicoesFilter(@Value("${app.seguranca.limite-requisicoes.por-minuto:30}") int limitePorMinuto,
                                   EscritorErroResposta escritor) {
        this(limitePorMinuto, Clock.systemUTC(), escritor);
    }

    LimiteRequisicoesFilter(int limitePorMinuto, Clock relogio, EscritorErroResposta escritor) {
        this.limitePorMinuto = limitePorMinuto;
        this.relogio = relogio;
        this.escritor = escritor;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String caminho = request.getRequestURI();
        return !(caminho.equals("/api/auth/login") || caminho.startsWith("/api/publico/"));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        long agora = relogio.millis();
        if (janelas.size() > MAXIMO_CHAVES) {
            janelas.values().removeIf(janela -> agora - janela.inicio() >= JANELA_MS);
        }
        String grupo = request.getRequestURI().startsWith("/api/auth") ? "auth" : "publico";
        Janela janela = janelas.compute(request.getRemoteAddr() + "|" + grupo, (chave, atual) ->
                atual == null || agora - atual.inicio() >= JANELA_MS ? new Janela(agora, new AtomicInteger()) : atual);

        if (janela.contador().incrementAndGet() > limitePorMinuto) {
            long segundosRestantes = (JANELA_MS - (agora - janela.inicio())) / 1000 + 1;
            response.setHeader(HttpHeaders.RETRY_AFTER, String.valueOf(segundosRestantes));
            escritor.escrever(response, TipoErro.MUITAS_REQUISICOES, "Muitas tentativas. Aguarde e tente novamente.");
            return;
        }
        chain.doFilter(request, response);
    }
}
