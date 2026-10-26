package br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.usecase;

import br.com.fiap.oficina_mecanica.compartilhado.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.dto.OrcamentoMapper;
import br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.dto.OrcamentoOutput;
import br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.usecase.VerificadorAcessoCliente;
import br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.dto.OrdemServicoClienteOutput;
import br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.dto.VeiculoDetalheOutput;
import br.com.fiap.oficina_mecanica.ordemservico.domain.orcamento.Orcamento;
import br.com.fiap.oficina_mecanica.ordemservico.domain.ordemservico.OrdemServico;
import br.com.fiap.oficina_mecanica.ordemservico.domain.ordemservico.OrdemServicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ConsultarOrdemServicoClienteUseCase {

    private final OrdemServicoRepository repository;
    private final VerificadorAcessoCliente verificadorAcesso;
    private final ConsultorVeiculos consultorVeiculos;

    public ConsultarOrdemServicoClienteUseCase(OrdemServicoRepository repository,
                                               VerificadorAcessoCliente verificadorAcesso,
                                               ConsultorVeiculos consultorVeiculos) {
        this.repository = repository;
        this.verificadorAcesso = verificadorAcesso;
        this.consultorVeiculos = consultorVeiculos;
    }

    @Transactional(readOnly = true)
    public OrdemServicoClienteOutput execute(UUID ordemServicoId, String cpf, String codigoAcesso) {
        verificadorAcesso.validar(ordemServicoId, cpf, codigoAcesso);

        OrdemServico ordemServico = repository.buscarPorId(ordemServicoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ordem de Serviço não encontrada."));

        VeiculoDetalheOutput veiculo = consultorVeiculos.detalharPorId(ordemServico.getVeiculoId()).orElse(null);
        // Transação somente leitura: a expiração só reflete no retorno, sem persistir.
        List<OrcamentoOutput> orcamentos = ordemServico.getOrcamentos().stream()
                .peek(Orcamento::verificarExpiracao)
                .map(OrcamentoMapper::toOutput)
                .toList();

        return OrdemServicoClienteOutput.de(ordemServico, veiculo, orcamentos);
    }
}
