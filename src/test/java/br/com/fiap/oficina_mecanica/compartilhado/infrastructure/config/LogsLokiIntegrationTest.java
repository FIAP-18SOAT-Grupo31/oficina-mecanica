package br.com.fiap.oficina_mecanica.compartilhado.infrastructure.config;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles({"test", "loki"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class LogsLokiIntegrationTest {

    private static final CountDownLatch PRIMEIRO_LOTE = new CountDownLatch(1);
    private static final HttpServer LOKI_FALSO = iniciarLokiFalso();

    static {
        System.setProperty("app.logs.loki.url",
                "http://localhost:" + LOKI_FALSO.getAddress().getPort() + "/loki/api/v1/push");
    }

    private static HttpServer iniciarLokiFalso() {
        try {
            HttpServer servidor = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
            servidor.createContext("/loki/api/v1/push", troca -> {
                troca.getRequestBody().readAllBytes();
                PRIMEIRO_LOTE.countDown();
                troca.sendResponseHeaders(204, -1);
                troca.close();
            });
            servidor.start();
            return servidor;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @AfterAll
    static void pararLoki() {
        LoggerContext contexto = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger raiz = contexto.getLogger(Logger.ROOT_LOGGER_NAME);
        var loki = raiz.getAppender("LOKI");
        if (loki != null) {
            raiz.detachAppender(loki);
            loki.stop();
        }
        LOKI_FALSO.stop(0);
        System.clearProperty("app.logs.loki.url");
    }

    @Test
    void perfilLokiEnviaOsLogsParaOLoki() throws InterruptedException {
        LoggerContext contexto = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger raiz = contexto.getLogger(Logger.ROOT_LOGGER_NAME);
        assertThat(raiz.getAppender("LOKI")).isNotNull();

        LoggerFactory.getLogger(LogsLokiIntegrationTest.class).info("log de teste para o Loki");

        assertThat(PRIMEIRO_LOTE.await(15, TimeUnit.SECONDS)).as("Loki recebeu um lote de logs").isTrue();
    }
}
