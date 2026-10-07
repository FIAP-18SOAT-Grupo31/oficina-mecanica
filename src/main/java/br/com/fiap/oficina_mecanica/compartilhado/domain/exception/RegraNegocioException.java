package br.com.fiap.oficina_mecanica.compartilhado.domain.exception;

public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
