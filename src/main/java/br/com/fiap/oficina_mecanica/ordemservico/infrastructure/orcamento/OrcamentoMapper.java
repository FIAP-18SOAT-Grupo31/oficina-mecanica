package br.com.fiap.oficina_mecanica.ordemservico.infrastructure.orcamento;

import br.com.fiap.oficina_mecanica.ordemservico.domain.orcamento.Orcamento;
import br.com.fiap.oficina_mecanica.ordemservico.domain.orcamento.ItemPeca;
import br.com.fiap.oficina_mecanica.ordemservico.domain.orcamento.ItemServico;
import br.com.fiap.oficina_mecanica.ordemservico.infrastructure.orcamento.entity.ItemPecaEntity;
import br.com.fiap.oficina_mecanica.ordemservico.infrastructure.orcamento.entity.ItemServicoEntity;
import br.com.fiap.oficina_mecanica.ordemservico.infrastructure.orcamento.entity.OrcamentoEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrcamentoMapper {

    public OrcamentoEntity toEntity(Orcamento dominio) {
        OrcamentoEntity entity = new OrcamentoEntity(
                dominio.getId(),
                dominio.getOrdemServicoId(),
                dominio.calcularValorTotal(),
                dominio.getStatus(),
                dominio.getDataCriacao(),
                dominio.getDataValidade()
        );

        List<ItemServicoEntity> servicosEntity = dominio.getServicos().stream()
                .map(s -> new ItemServicoEntity(s.catalogoServicoId(), s.descricao(), s.valorMaoDeObra()))
                .toList();
        entity.setServicos(servicosEntity);

        List<ItemPecaEntity> pecasEntity = dominio.getPecas().stream()
                .map(p -> new ItemPecaEntity(p.produtoEstoqueId(), p.nomePeca(), p.quantidade(), p.valorUnitario()))
                .toList();
        entity.setPecas(pecasEntity);

        return entity;
    }

    public Orcamento toDomain(OrcamentoEntity entity) {
        List<ItemServico> servicos = entity.getServicos().stream()
                .map(this::toServicoDomain)
                .toList();

        List<ItemPeca> pecas = entity.getPecas().stream()
                .map(this::toPecaDomain)
                .toList();

        return new Orcamento(
                entity.getId(),
                entity.getOrdemServicoId(),
                entity.getDataCriacao(),
                entity.getDataValidade(),
                entity.getStatus(),
                servicos,
                pecas
        );
    }

    private ItemServico toServicoDomain(ItemServicoEntity entity) {
        return new ItemServico(entity.getCatalogoServicoId(), entity.getDescricao(), entity.getValorMaoDeObra());
    }

    private ItemPeca toPecaDomain(ItemPecaEntity entity) {
        return new ItemPeca(entity.getProdutoEstoqueId(), entity.getNomePeca(), entity.getQuantidade(), entity.getValorUnitario());
    }
}