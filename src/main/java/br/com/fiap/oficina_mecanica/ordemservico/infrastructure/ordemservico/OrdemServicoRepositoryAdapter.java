package br.com.fiap.oficina_mecanica.ordemservico.infrastructure.ordemservico;

import br.com.fiap.oficina_mecanica.ordemservico.domain.orcamento.Orcamento;
import br.com.fiap.oficina_mecanica.ordemservico.domain.orcamento.OrcamentoRepository;
import br.com.fiap.oficina_mecanica.ordemservico.domain.ordemservico.OrdemServico;
import br.com.fiap.oficina_mecanica.ordemservico.domain.ordemservico.OrdemServicoRepository;
import br.com.fiap.oficina_mecanica.ordemservico.infrastructure.ordemservico.entity.OrdemServicoEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class OrdemServicoRepositoryAdapter implements OrdemServicoRepository {

    private final OrdemServicoJpaRepository jpaRepository;
    private final OrdemServicoMapper mapper;
    private final OrcamentoRepository orcamentoRepository;

    public OrdemServicoRepositoryAdapter(OrdemServicoJpaRepository jpaRepository, OrdemServicoMapper mapper,
                                         OrcamentoRepository orcamentoRepository) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
        this.orcamentoRepository = orcamentoRepository;
    }

    @Override
    public OrdemServico salvar(OrdemServico ordemServico) {
        OrdemServicoEntity savedEntity = jpaRepository.save(mapper.toEntity(ordemServico));
        return mapper.toDomain(savedEntity, ordemServico.getOrcamentos());
    }

    @Override
    public Optional<OrdemServico> buscarPorId(UUID id) {
        return jpaRepository.findById(id)
                .map(entity -> mapper.toDomain(entity, orcamentosDa(entity.getId())));
    }

    private List<Orcamento> orcamentosDa(UUID ordemServicoId) {
        return orcamentoRepository.buscarPorOrdemServicoId(ordemServicoId).stream().toList();
    }
}
