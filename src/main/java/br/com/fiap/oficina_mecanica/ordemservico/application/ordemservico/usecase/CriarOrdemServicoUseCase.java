package br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.usecase;

import br.com.fiap.oficina_mecanica.compartilhado.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.dto.CriarOrdemServicoInput;
import br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.dto.CriarOrdemServicoOutput;
import br.com.fiap.oficina_mecanica.ordemservico.domain.ordemservico.OrdemServico;
import br.com.fiap.oficina_mecanica.ordemservico.domain.ordemservico.OrdemServicoRepository;
import com.github.f4b6a3.uuid.UuidCreator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        if (!verificadorCliente.existe(input.clienteId())) {
            throw new RecursoNaoEncontradoException("Cliente não encontrado.");
        }

        if (!verificadorVeiculo.existe(input.veiculoId())) {
            throw new RecursoNaoEncontradoException("Veículo não encontrado.");
        }

        OrdemServico ordemServico = new OrdemServico(
                UuidCreator.getTimeOrderedEpoch(), input.clienteId(), input.veiculoId(), input.relatoProblema());

        OrdemServico ordemServicoSalva = repository.salvar(ordemServico);

        return new CriarOrdemServicoOutput(ordemServicoSalva.getId());
    }
}
