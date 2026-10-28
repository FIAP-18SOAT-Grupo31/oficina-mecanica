package br.com.fiap.oficina_mecanica.servico.application.usecase;

import br.com.fiap.oficina_mecanica.compartilhado.domain.exception.RegraNegocioException;
import br.com.fiap.oficina_mecanica.servico.application.dto.AtualizarServicoInput;
import br.com.fiap.oficina_mecanica.servico.application.dto.ServicoOutput;
import br.com.fiap.oficina_mecanica.servico.domain.Servico;
import br.com.fiap.oficina_mecanica.servico.domain.ServicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AtualizarServicoUseCase {

    private final ServicoRepository repository;

    public AtualizarServicoUseCase(ServicoRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public ServicoOutput execute(UUID id, AtualizarServicoInput input) {
        Servico servico = repository.buscarPorId(id).orElseThrow(ServicoNaoEncontradoException::new);

        atualizarDados(servico, input);

        if (repository.existeOutroComNome(servico.getNome(), id)) {
            throw new RegraNegocioException("Já existe outro serviço cadastrado com este nome.");
        }

        return ServicoOutput.de(repository.salvar(servico));
    }

    private static void atualizarDados(Servico servico, AtualizarServicoInput input) {
        try {
            servico.atualizar(input.nome(), input.descricao(), input.valorMaoDeObra());
        } catch (IllegalArgumentException ex) {
            throw new RegraNegocioException(ex.getMessage());
        }
    }
}
