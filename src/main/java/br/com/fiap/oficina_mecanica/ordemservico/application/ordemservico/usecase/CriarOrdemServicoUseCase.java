package br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.usecase;

import br.com.fiap.oficina_mecanica.compartilhado.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.oficina_mecanica.compartilhado.domain.exception.RegraNegocioException;
import br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.dto.CriarOrdemServicoInput;
import br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.dto.CriarOrdemServicoOutput;
import br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.usecase.VerificadorVeiculo.VeiculoCadastrado;
import br.com.fiap.oficina_mecanica.ordemservico.domain.ordemservico.OrdemServico;
import br.com.fiap.oficina_mecanica.ordemservico.domain.ordemservico.OrdemServicoRepository;
import com.github.f4b6a3.uuid.UuidCreator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CriarOrdemServicoUseCase {

    private final VerificadorCliente verificadorCliente;
    private final VerificadorVeiculo verificadorVeiculo;
    private final OrdemServicoRepository repository;

    public CriarOrdemServicoUseCase(VerificadorCliente verificadorCliente, VerificadorVeiculo verificadorVeiculo,
                                    OrdemServicoRepository repository) {
        this.verificadorCliente = verificadorCliente;
        this.verificadorVeiculo = verificadorVeiculo;
        this.repository = repository;
    }

    @Transactional
    public CriarOrdemServicoOutput execute(CriarOrdemServicoInput input) {
        UUID clienteId = identificarCliente(input);
        VeiculoCadastrado veiculo = identificarVeiculo(input);

        if (!veiculo.clienteId().equals(clienteId)) {
            throw new RegraNegocioException("O veículo informado não pertence ao cliente.");
        }

        OrdemServico ordemServico = new OrdemServico(
                UuidCreator.getTimeOrderedEpoch(), clienteId, veiculo.id(), input.relatoProblema());

        OrdemServico ordemServicoSalva = repository.salvar(ordemServico);

        return new CriarOrdemServicoOutput(ordemServicoSalva.getId());
    }

    private UUID identificarCliente(CriarOrdemServicoInput input) {
        if (input.clienteId() != null) {
            if (!verificadorCliente.existe(input.clienteId())) {
                throw new RecursoNaoEncontradoException("Cliente não encontrado.");
            }
            return input.clienteId();
        }
        return verificadorCliente.buscarIdPorDocumento(normalizar(input.documentoCliente()))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado para o CPF/CNPJ informado."));
    }

    private VeiculoCadastrado identificarVeiculo(CriarOrdemServicoInput input) {
        if (input.veiculoId() != null) {
            return verificadorVeiculo.buscarPorId(input.veiculoId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Veículo não encontrado."));
        }
        return verificadorVeiculo.buscarPorPlaca(normalizar(input.placaVeiculo()))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Veículo não encontrado para a placa informada."));
    }

    private static String normalizar(String valor) {
        return valor.replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
    }
}
