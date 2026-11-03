package br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.usecase;

import br.com.fiap.oficina_mecanica.compartilhado.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.dto.OrcamentoMapper;
import br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.dto.OrcamentoOutput;
import br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.dto.ClienteDetalheOutput;
import br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.dto.OrdemServicoDetalheOutput;
import br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.dto.VeiculoDetalheOutput;
import br.com.fiap.oficina_mecanica.ordemservico.domain.ordemservico.OrdemServico;
import br.com.fiap.oficina_mecanica.ordemservico.domain.ordemservico.OrdemServicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class BuscarOrdemServicoUseCase {

    private final OrdemServicoRepository repository;
    private final ConsultorClientes consultorClientes;
    private final ConsultorVeiculos consultorVeiculos;

    public BuscarOrdemServicoUseCase(OrdemServicoRepository repository, ConsultorClientes consultorClientes,
                                     ConsultorVeiculos consultorVeiculos) {
        this.repository = repository;
        this.consultorClientes = consultorClientes;
        this.consultorVeiculos = consultorVeiculos;
    }

    @Transactional(readOnly = true)
    public OrdemServicoDetalheOutput execute(UUID ordemServicoId) {
        OrdemServico ordemServico = repository.buscarPorId(ordemServicoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ordem de Serviço não encontrada."));

        ClienteDetalheOutput cliente = consultorClientes.detalharPorId(ordemServico.getClienteId()).orElse(null);
        VeiculoDetalheOutput veiculo = consultorVeiculos.detalharPorId(ordemServico.getVeiculoId()).orElse(null);
        List<OrcamentoOutput> orcamentos = ordemServico.getOrcamentos().stream()
                .map(OrcamentoMapper::toOutput)
                .toList();

        return OrdemServicoDetalheOutput.de(ordemServico, cliente, veiculo, orcamentos);
    }
}
