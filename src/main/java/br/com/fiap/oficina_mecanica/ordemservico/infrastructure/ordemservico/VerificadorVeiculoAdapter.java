package br.com.fiap.oficina_mecanica.ordemservico.infrastructure.ordemservico;

import br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.usecase.VerificadorVeiculo;
import br.com.fiap.oficina_mecanica.veiculo.domain.Veiculo;
import br.com.fiap.oficina_mecanica.veiculo.domain.VeiculoRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class VerificadorVeiculoAdapter implements VerificadorVeiculo {

    private final VeiculoRepository veiculoRepository;

    public VerificadorVeiculoAdapter(VeiculoRepository veiculoRepository) {
        this.veiculoRepository = veiculoRepository;
    }

    @Override
    public Optional<VeiculoCadastrado> buscarPorId(UUID veiculoId) {
        return veiculoRepository.buscarPorId(veiculoId).map(VerificadorVeiculoAdapter::paraCadastrado);
    }

    @Override
    public Optional<VeiculoCadastrado> buscarPorPlaca(String placa) {
        return veiculoRepository.listar(null, placa, null, 0, 1).conteudo().stream()
                .findFirst()
                .map(VerificadorVeiculoAdapter::paraCadastrado);
    }

    private static VeiculoCadastrado paraCadastrado(Veiculo veiculo) {
        return new VeiculoCadastrado(veiculo.getId(), veiculo.getClienteId());
    }
}
