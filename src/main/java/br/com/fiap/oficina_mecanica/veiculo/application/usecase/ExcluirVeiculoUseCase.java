package br.com.fiap.oficina_mecanica.veiculo.application.usecase;

import br.com.fiap.oficina_mecanica.compartilhado.domain.exception.RegraNegocioException;
import br.com.fiap.oficina_mecanica.veiculo.domain.VeiculoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ExcluirVeiculoUseCase {

    private final VeiculoRepository repository;
    private final VerificadorOrdensServicoDoVeiculo verificadorOrdensServico;

    public ExcluirVeiculoUseCase(VeiculoRepository repository,
                                 VerificadorOrdensServicoDoVeiculo verificadorOrdensServico) {
        this.repository = repository;
        this.verificadorOrdensServico = verificadorOrdensServico;
    }

    @Transactional
    public void execute(UUID id) {
        if (!repository.existePorId(id)) {
            throw new VeiculoNaoEncontradoException();
        }

        if (verificadorOrdensServico.possuiOrdensServico(id)) {
            throw new RegraNegocioException(
                    "Não é possível excluir o veículo: existem ordens de serviço vinculadas a ele.");
        }

        repository.excluirPorId(id);
    }
}
