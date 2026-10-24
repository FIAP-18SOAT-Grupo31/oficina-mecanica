package br.com.fiap.oficina_mecanica.cliente.application.usecase;

import br.com.fiap.oficina_mecanica.cliente.domain.ClienteRepository;
import br.com.fiap.oficina_mecanica.compartilhado.domain.exception.RegraNegocioException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ExcluirClienteUseCase {

    private final ClienteRepository repository;
    private final VerificadorVeiculosDoCliente verificadorVeiculos;

    public ExcluirClienteUseCase(ClienteRepository repository, VerificadorVeiculosDoCliente verificadorVeiculos) {
        this.repository = repository;
        this.verificadorVeiculos = verificadorVeiculos;
    }

    @Transactional
    public void execute(UUID id) {
        if (!repository.existePorId(id)) {
            throw new ClienteNaoEncontradoException();
        }

        if (verificadorVeiculos.possuiVeiculos(id)) {
            throw new RegraNegocioException("Não é possível excluir o cliente: existem veículos vinculados a ele.");
        }

        repository.excluirPorId(id);
    }
}
