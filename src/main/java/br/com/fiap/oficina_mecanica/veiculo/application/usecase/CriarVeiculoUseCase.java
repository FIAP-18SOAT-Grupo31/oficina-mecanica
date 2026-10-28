package br.com.fiap.oficina_mecanica.veiculo.application.usecase;

import br.com.fiap.oficina_mecanica.compartilhado.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.oficina_mecanica.compartilhado.domain.exception.RegraNegocioException;
import br.com.fiap.oficina_mecanica.veiculo.application.dto.CriarVeiculoInput;
import br.com.fiap.oficina_mecanica.veiculo.application.dto.VeiculoOutput;
import br.com.fiap.oficina_mecanica.veiculo.domain.Veiculo;
import br.com.fiap.oficina_mecanica.veiculo.domain.VeiculoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CriarVeiculoUseCase {

    private final VeiculoRepository repository;
    private final VerificadorDonoDoVeiculo verificadorDono;

    public CriarVeiculoUseCase(VeiculoRepository repository, VerificadorDonoDoVeiculo verificadorDono) {
        this.repository = repository;
        this.verificadorDono = verificadorDono;
    }

    @Transactional
    public VeiculoOutput execute(CriarVeiculoInput input) {
        Veiculo veiculo = montarVeiculo(input);

        if (!verificadorDono.existe(veiculo.getClienteId())) {
            throw new RecursoNaoEncontradoException("Cliente não encontrado.");
        }

        if (repository.existePorPlaca(veiculo.getPlaca())) {
            throw new RegraNegocioException("Já existe um veículo cadastrado com esta placa.");
        }

        return VeiculoOutput.de(repository.salvar(veiculo));
    }

    private static Veiculo montarVeiculo(CriarVeiculoInput input) {
        try {
            return new Veiculo(
                    input.anoFabricacao() == null ? 0 : input.anoFabricacao(),
                    input.placa(),
                    input.modelo(),
                    input.marca(),
                    input.clienteId()
            );
        } catch (IllegalArgumentException ex) {
            throw new RegraNegocioException(ex.getMessage());
        }
    }
}
