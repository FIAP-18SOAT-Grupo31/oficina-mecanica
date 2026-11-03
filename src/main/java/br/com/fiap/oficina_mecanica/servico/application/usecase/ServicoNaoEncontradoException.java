package br.com.fiap.oficina_mecanica.servico.application.usecase;

import br.com.fiap.oficina_mecanica.compartilhado.domain.exception.RecursoNaoEncontradoException;

public class ServicoNaoEncontradoException extends RecursoNaoEncontradoException {

    public ServicoNaoEncontradoException() {
        super("Serviço não encontrado.");
    }
}
