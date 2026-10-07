package br.com.fiap.oficina_mecanica.compartilhado.application.exception;

public class NaoAutenticadoException extends RuntimeException {

    public NaoAutenticadoException(String mensagem) {
        super(mensagem);
    }
}
