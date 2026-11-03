package br.com.fiap.oficina_mecanica.veiculo.application.usecase;

import br.com.fiap.oficina_mecanica.compartilhado.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.oficina_mecanica.compartilhado.domain.exception.RegraNegocioException;
import br.com.fiap.oficina_mecanica.veiculo.application.dto.AtualizarVeiculoInput;
import br.com.fiap.oficina_mecanica.veiculo.application.dto.VeiculoOutput;
import br.com.fiap.oficina_mecanica.veiculo.domain.Veiculo;
import br.com.fiap.oficina_mecanica.veiculo.domain.VeiculoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AtualizarVeiculoUseCase {

    private final VeiculoRepository repository;
    private final VerificadorDonoDoVeiculo verificadorDono;

    public AtualizarVeiculoUseCase(VeiculoRepository repository, VerificadorDonoDoVeiculo verificadorDono) {
        this.repository = repository;
        this.verificadorDono = verificadorDono;
    }

    @Transactional
    public VeiculoOutput execute(UUID id, AtualizarVeiculoInput input) {
        Veiculo veiculo = repository.buscarPorId(id).orElseThrow(VeiculoNaoEncontradoException::new);

        atualizarDados(veiculo, input);

        if (!verificadorDono.existe(veiculo.getClienteId())) {
            throw new RecursoNaoEncontradoException("Cliente não encontrado.");
        }

        if (repository.existeOutroComPlaca(veiculo.getPlaca(), id)) {
            throw new RegraNegocioException("Já existe outro veículo cadastrado com esta placa.");
        }

        return VeiculoOutput.de(repository.salvar(veiculo));
    }

    private static void atualizarDados(Veiculo veiculo, AtualizarVeiculoInput input) {
        try {
            veiculo.atualizar(
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
