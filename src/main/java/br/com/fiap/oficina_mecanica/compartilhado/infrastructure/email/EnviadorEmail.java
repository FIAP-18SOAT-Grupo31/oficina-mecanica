package br.com.fiap.oficina_mecanica.compartilhado.infrastructure.email;

public interface EnviadorEmail {
    void enviar(String destinatario, String assunto, String corpo);
}
