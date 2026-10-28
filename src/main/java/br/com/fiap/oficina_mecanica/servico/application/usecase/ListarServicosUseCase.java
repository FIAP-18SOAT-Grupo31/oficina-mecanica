package br.com.fiap.oficina_mecanica.servico.application.usecase;

import br.com.fiap.oficina_mecanica.compartilhado.application.dto.PaginaOutput;
import br.com.fiap.oficina_mecanica.servico.application.dto.ListarServicosInput;
import br.com.fiap.oficina_mecanica.servico.application.dto.ServicoOutput;
import br.com.fiap.oficina_mecanica.servico.domain.ServicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListarServicosUseCase {

    private final ServicoRepository repository;

    public ListarServicosUseCase(ServicoRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public PaginaOutput<ServicoOutput> execute(ListarServicosInput input) {
        return PaginaOutput.de(
                repository.listar(normalizarNome(input.nome()), input.pagina(), input.tamanho())
                        .mapear(ServicoOutput::de)
        );
    }

    private static String normalizarNome(String nome) {
        if (nome == null || nome.isBlank()) {
            return null;
        }
        return nome.trim();
    }
}
