package br.com.fiap.oficina_mecanica.ordemservico.infrastructure.ordemservico;

import br.com.fiap.oficina_mecanica.ordemservico.domain.orcamento.Orcamento;
import br.com.fiap.oficina_mecanica.ordemservico.domain.ordemservico.OrdemServico;
import br.com.fiap.oficina_mecanica.ordemservico.infrastructure.ordemservico.entity.OrdemServicoEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class OrdemServicoMapper {

    public OrdemServicoEntity toEntity(OrdemServico dominio) {
        return new OrdemServicoEntity(
                dominio.getId(),
                dominio.getClienteId(),
                dominio.getVeiculoId(),
                dominio.getRelatoProblema(),
                dominio.getStatus(),
                dominio.getDataAbertura(),
                dominio.getDataConclusao(),
                dominio.getCodigoAcesso()
        );
    }

    public OrdemServico toDomain(OrdemServicoEntity entity, List<Orcamento> orcamentos) {
        return OrdemServico.builder()
                .id(entity.getId())
                .clienteId(entity.getClienteId())
                .veiculoId(entity.getVeiculoId())
                .relatoProblema(entity.getRelatoProblema())
                .status(entity.getStatus())
                .dataAbertura(entity.getDataAbertura())
                .dataConclusao(entity.getDataConclusao())
                .codigoAcesso(entity.getCodigoAcesso())
                .orcamentos(new ArrayList<>(orcamentos))
                .build();
    }
}
