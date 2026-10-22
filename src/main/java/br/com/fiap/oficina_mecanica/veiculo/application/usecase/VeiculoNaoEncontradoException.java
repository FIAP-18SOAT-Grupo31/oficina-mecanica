package br.com.fiap.oficina_mecanica.veiculo.application.usecase;

import br.com.fiap.oficina_mecanica.compartilhado.domain.exception.RecursoNaoEncontradoException;

public class VeiculoNaoEncontradoException extends RecursoNaoEncontradoException {

    public VeiculoNaoEncontradoException() {
        super("Veículo não encontrado.");
    }
}
