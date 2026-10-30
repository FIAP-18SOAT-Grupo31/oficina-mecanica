package br.com.fiap.oficina_mecanica.compartilhado.infrastructure.email;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

class EnviadorEmailLogTest {

    private final ListAppender<ILoggingEvent> logs = new ListAppender<>();
    private final Logger logger = (Logger) LoggerFactory.getLogger(EnviadorEmailLog.class);

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
    void registraDestinatarioAssuntoECorpoNoLog() {
        new EnviadorEmailLog().enviar("cliente@exemplo.com", "Assunto", "Corpo da mensagem");

        assertThat(logs.list).hasSize(1);
        assertThat(logs.list.getFirst().getFormattedMessage())
                .contains("SIMULAÇÃO DE E-MAIL")
                .contains("Assunto")
                .contains("Para: cliente@exemplo.com")
                .contains("Corpo da mensagem");
    }
}
