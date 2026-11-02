package br.com.fiap.oficina_mecanica.compartilhado.infrastructure.email;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EnviadorEmailSmtpTest {

    @Mock
    private JavaMailSender mailSender;

    @Test
    void enviaAMensagemComRemetenteDestinatarioAssuntoETexto() {
        new EnviadorEmailSmtp(mailSender, "oficina@exemplo.com")
                .enviar("cliente@exemplo.com", "Assunto", "Corpo da mensagem");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        SimpleMailMessage mensagem = captor.getValue();
        assertThat(mensagem.getFrom()).isEqualTo("oficina@exemplo.com");
        assertThat(mensagem.getTo()).containsExactly("cliente@exemplo.com");
        assertThat(mensagem.getSubject()).isEqualTo("Assunto");
        assertThat(mensagem.getText()).isEqualTo("Corpo da mensagem");
    }
}
