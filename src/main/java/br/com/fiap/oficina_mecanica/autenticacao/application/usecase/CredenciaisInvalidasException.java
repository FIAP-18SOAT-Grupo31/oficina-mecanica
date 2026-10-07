package br.com.fiap.oficina_mecanica.autenticacao.application.usecase;

import br.com.fiap.oficina_mecanica.compartilhado.application.exception.NaoAutenticadoException;

public class CredenciaisInvalidasException extends NaoAutenticadoException {

    public CredenciaisInvalidasException() {
        super("Usuário ou senha inválidos.");
    }
}
