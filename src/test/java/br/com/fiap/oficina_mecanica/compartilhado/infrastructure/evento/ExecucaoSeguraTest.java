package br.com.fiap.oficina_mecanica.compartilhado.infrastructure.evento;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class ExecucaoSeguraTest {

    private final ListAppender<ILoggingEvent> logs = new ListAppender<>();
    private final Logger logger = (Logger) LoggerFactory.getLogger(ExecucaoSeguraTest.class);

    @BeforeEach
    void anexarAppender() {
        logs.start();
        logger.addAppender(logs);
    }

    @AfterEach
    void removerAppender() {
        logger.detachAppender(logs);
    }

    @Test
    void executaAAcaoSemRegistrarErro() {
        AtomicBoolean executou = new AtomicBoolean(false);

        ExecucaoSegura.executar(logger, "o aviso", () -> executou.set(true));

        assertThat(executou).isTrue();
        assertThat(logs.list).isEmpty();
    }

    @Test
    void engoleAFalhaERegistraErroComADescricao() {
        assertThatCode(() -> ExecucaoSegura.executar(logger, "o aviso", () -> {
            throw new IllegalStateException("falhou");
        })).doesNotThrowAnyException();

        assertThat(logs.list).hasSize(1);
        assertThat(logs.list.getFirst().getLevel()).isEqualTo(Level.ERROR);
        assertThat(logs.list.getFirst().getFormattedMessage()).contains("o aviso");
        assertThat(logs.list.getFirst().getThrowableProxy().getMessage()).isEqualTo("falhou");
    }
}
