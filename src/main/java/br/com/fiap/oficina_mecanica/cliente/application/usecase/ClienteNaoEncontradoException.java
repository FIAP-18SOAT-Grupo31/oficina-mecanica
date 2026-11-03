package br.com.fiap.oficina_mecanica.cliente.application.usecase;

import br.com.fiap.oficina_mecanica.compartilhado.domain.exception.RecursoNaoEncontradoException;

public class ClienteNaoEncontradoException extends RecursoNaoEncontradoException {

    public ClienteNaoEncontradoException() {
        super("Cliente não encontrado.");
    }
}
