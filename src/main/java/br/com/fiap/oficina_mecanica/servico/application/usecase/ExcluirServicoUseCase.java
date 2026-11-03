package br.com.fiap.oficina_mecanica.servico.application.usecase;

import br.com.fiap.oficina_mecanica.compartilhado.domain.exception.RegraNegocioException;
import br.com.fiap.oficina_mecanica.servico.domain.ServicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ExcluirServicoUseCase {

    private final ServicoRepository repository;
    private final VerificadorUsoDoServico verificadorUso;

    public ExcluirServicoUseCase(ServicoRepository repository, VerificadorUsoDoServico verificadorUso) {
        this.repository = repository;
        this.verificadorUso = verificadorUso;
    }

    @Transactional
    public void execute(UUID id) {
        if (!repository.existePorId(id)) {
            throw new ServicoNaoEncontradoException();
        }

        if (verificadorUso.usadoEmOrcamento(id)) {
            throw new RegraNegocioException("Não é possível excluir o serviço: ele já foi usado em orçamentos.");
        }

        repository.excluirPorId(id);
    }
}
