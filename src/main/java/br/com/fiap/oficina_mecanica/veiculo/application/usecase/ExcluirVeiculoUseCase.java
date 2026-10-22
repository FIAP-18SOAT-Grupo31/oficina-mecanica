package br.com.fiap.oficina_mecanica.veiculo.application.usecase;

import br.com.fiap.oficina_mecanica.veiculo.domain.VeiculoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ExcluirVeiculoUseCase {

    private final VeiculoRepository repository;

    public ExcluirVeiculoUseCase(VeiculoRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void execute(UUID id) {
        if (!repository.existePorId(id)) {
            throw new VeiculoNaoEncontradoException();
        }

        repository.excluirPorId(id);
    }
}
