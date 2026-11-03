package br.com.fiap.oficina_mecanica.cliente.application.usecase;

import br.com.fiap.oficina_mecanica.cliente.application.dto.ClienteOutput;
import br.com.fiap.oficina_mecanica.cliente.application.dto.ListarClientesInput;
import br.com.fiap.oficina_mecanica.cliente.domain.ClienteRepository;
import br.com.fiap.oficina_mecanica.compartilhado.application.dto.PaginaOutput;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListarClientesUseCase {

    private final ClienteRepository repository;

    public ListarClientesUseCase(ClienteRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public PaginaOutput<ClienteOutput> execute(ListarClientesInput input) {
        return PaginaOutput.de(
                repository.listar(
                        normalizarNome(input.nome()),
                        normalizarDocumento(input.documento()),
                        input.pagina(),
                        input.tamanho()
                ).mapear(ClienteOutput::de)
        );
    }

    private static String normalizarNome(String nome) {
        if (nome == null || nome.isBlank()) {
            return null;
        }
        return nome.trim();
    }

    private static String normalizarDocumento(String documento) {
        if (documento == null || documento.isBlank()) {
            return null;
        }
        return documento.replaceAll("\\W", "").toUpperCase();
    }
}
