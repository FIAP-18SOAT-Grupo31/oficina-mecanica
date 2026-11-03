package br.com.fiap.oficina_mecanica.cliente.application.usecase;

import br.com.fiap.oficina_mecanica.cliente.application.dto.AtualizarClienteInput;
import br.com.fiap.oficina_mecanica.cliente.application.dto.ClienteOutput;
import br.com.fiap.oficina_mecanica.cliente.application.dto.EnderecoInput;
import br.com.fiap.oficina_mecanica.cliente.domain.Cliente;
import br.com.fiap.oficina_mecanica.cliente.domain.ClienteRepository;
import br.com.fiap.oficina_mecanica.compartilhado.domain.exception.RegraNegocioException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AtualizarClienteUseCase {

    private final ClienteRepository repository;

    public AtualizarClienteUseCase(ClienteRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public ClienteOutput execute(UUID id, AtualizarClienteInput input) {
        Cliente cliente = repository.buscarPorId(id).orElseThrow(ClienteNaoEncontradoException::new);

        atualizarDados(cliente, input);

        if (repository.existeOutroComDocumento(cliente.getDocumento(), id)) {
            throw new RegraNegocioException("Já existe outro cliente cadastrado com este documento.");
        }

        return ClienteOutput.de(repository.salvar(cliente));
    }

    private static void atualizarDados(Cliente cliente, AtualizarClienteInput input) {
        try {
            cliente.atualizar(
                    input.documento(),
                    input.nome(),
                    input.telefone(),
                    input.email(),
                    EnderecoInput.paraDominio(input.endereco())
            );
        } catch (IllegalArgumentException ex) {
            throw new RegraNegocioException(ex.getMessage());
        }
    }
}
