package br.com.fiap.oficina_mecanica.ordemservico.infrastructure.ordemservico;

import br.com.fiap.oficina_mecanica.ordemservico.domain.orcamento.Orcamento;
import br.com.fiap.oficina_mecanica.ordemservico.domain.ordemservico.OrdemServico;
import br.com.fiap.oficina_mecanica.ordemservico.infrastructure.orcamento.OrcamentoMapper;
import br.com.fiap.oficina_mecanica.ordemservico.infrastructure.orcamento.entity.OrcamentoEntity;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

@Component 
@RequiredArgsConstructor 
public class OrdemServicoMapper {

    private final OrcamentoMapper orcamentoMapper;

    public OrdemServicoEntity toEntity(OrdemServico dominio) {
        if (dominio == null) {
            return null;
        }
        
        List<OrcamentoEntity> orcamentos = new ArrayList<>();
        
        if (dominio.getOrcamentos() != null) {
            orcamentos = dominio.getOrcamentos().stream()
                    .map(o -> orcamentoMapper.toEntity(o))
                    .toList();
        }

        return OrdemServicoEntity.builder()
                .id(dominio.getId())
                .clienteId(dominio.getClienteId())
                .veiculoId(dominio.getVeiculoId())
                .relatoProblema(dominio.getRelatoProblema())
                .statusOS(dominio.getStatus())
                .dataAbertura(dominio.getDataAbertura())
                .dataConclusao(dominio.getDataConclusao())
                .orcamentos(orcamentos)
                .build();
    }

    public OrdemServico toDomain(OrdemServicoEntity entidade) {
        if (entidade == null) {
            return null;
        }

        List<Orcamento> orcamentos = new ArrayList<>();

        if (entidade.getOrcamentos() != null) {
            orcamentos = entidade.getOrcamentos().stream()
                    .map(o -> orcamentoMapper.toDomain(o))
                    .toList();
        }

        return OrdemServico.builder()
                .id(entidade.getId())
                .clienteId(entidade.getClienteId())
                .veiculoId(entidade.getVeiculoId())
                .relatoProblema(entidade.getRelatoProblema())
                .status(entidade.getStatusOS())
                .dataAbertura(entidade.getDataAbertura())
                .dataConclusao(entidade.getDataConclusao())
                .orcamentos(orcamentos)
                .build();
    }
}
