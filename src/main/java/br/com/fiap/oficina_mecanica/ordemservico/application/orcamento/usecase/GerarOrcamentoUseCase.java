package br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.usecase;

import br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.dto.GerarOrcamentoInput;
import br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.dto.OrcamentoOutput;
import br.com.fiap.oficina_mecanica.ordemservico.domain.orcamento.ItemPeca;
import br.com.fiap.oficina_mecanica.ordemservico.domain.orcamento.ItemServico;
import br.com.fiap.oficina_mecanica.ordemservico.domain.orcamento.Orcamento;
import br.com.fiap.oficina_mecanica.ordemservico.domain.orcamento.OrcamentoRepository;
import org.springframework.stereotype.Service;

@Service
public class GerarOrcamentoUseCase {

    private final OrcamentoRepository repository;

    public GerarOrcamentoUseCase(OrcamentoRepository repository) {
        this.repository = repository;
    }

    public OrcamentoOutput execute(GerarOrcamentoInput input) {
        Orcamento orcamento = new Orcamento(input.ordemServicoId(), input.dataValidade());

        if (input.servicos() != null) {
            input.servicos().forEach(s ->
                    orcamento.adicionarServico(new ItemServico(s.catalogoServicoId(), s.descricao(), s.valorMaoDeObra()))
            );
        }

        if (input.pecas() != null) {
            input.pecas().forEach(p ->
                    orcamento.adicionarPeca(new ItemPeca(p.produtoEstoqueId(), p.nomePeca(), p.quantidade(), p.valorUnitario()))
            );
        }

        Orcamento orcamentoSalvo = repository.salvar(orcamento);

        return new OrcamentoOutput(
                orcamentoSalvo.getId(),
                orcamentoSalvo.getOrdemServicoId(),
                orcamentoSalvo.calcularValorTotal(),
                orcamentoSalvo.getStatus(),
                orcamentoSalvo.getDataCriacao(),
                orcamentoSalvo.getDataValidade()
        );
    }
}