package br.com.fiap.oficina_mecanica.cliente.application.usecase;

import br.com.fiap.oficina_mecanica.cliente.application.dto.ClienteOutput;
import br.com.fiap.oficina_mecanica.cliente.application.dto.CriarClienteInput;
import br.com.fiap.oficina_mecanica.cliente.application.dto.EnderecoInput;
import br.com.fiap.oficina_mecanica.cliente.domain.Cliente;
import br.com.fiap.oficina_mecanica.cliente.domain.ClienteRepository;
import br.com.fiap.oficina_mecanica.compartilhado.domain.exception.RegraNegocioException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CriarClienteUseCase {

    private final ClienteRepository repository;

    public CriarClienteUseCase(ClienteRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public ClienteOutput execute(CriarClienteInput input) {
        Cliente cliente = montarCliente(input);

        if (repository.existePorDocumento(cliente.getDocumento())) {
            throw new RegraNegocioException("Já existe um cliente cadastrado com este documento.");
        }

        return ClienteOutput.de(repository.salvar(cliente));
    }

    private static Cliente montarCliente(CriarClienteInput input) {
        try {
            return new Cliente(
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
