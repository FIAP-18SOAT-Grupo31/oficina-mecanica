package br.com.fiap.oficina_mecanica.compartilhado.infrastructure.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** Usado quando o SMTP está desligado: apenas registra no log o e-mail que seria enviado. */
@Component
@ConditionalOnProperty(name = "app.email.smtp.habilitado", havingValue = "false", matchIfMissing = true)
public class EnviadorEmailLog implements EnviadorEmail {

    private static final Logger LOG = LoggerFactory.getLogger(EnviadorEmailLog.class);

    @Override
    public void enviar(String destinatario, String assunto, String corpo) {
        String corpoMascarado = corpo.replaceAll("(?im)(C[oó]digo de Acesso:\\s*)[^\\r\\n]+", "$1******");
        LOG.debug("\n=========================================================================" +
                        "\n📧 [SIMULAÇÃO DE E-MAIL] - {}" +
                        "\nPara: {}" +
                        "\n=========================================================================" +
                        "\n{}" +
                        "\n=========================================================================\n",
                assunto, destinatario, corpoMascarado);
    }
}
