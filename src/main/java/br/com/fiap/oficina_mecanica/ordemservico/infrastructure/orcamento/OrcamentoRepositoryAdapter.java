package br.com.fiap.oficina_mecanica.ordemservico.infrastructure.orcamento;

import br.com.fiap.oficina_mecanica.ordemservico.domain.orcamento.Orcamento;
import br.com.fiap.oficina_mecanica.ordemservico.domain.orcamento.OrcamentoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class OrcamentoRepositoryAdapter implements OrcamentoRepository {

    private final OrcamentoJpaRepository jpaRepository;
    private final OrcamentoMapper mapper;

    public OrcamentoRepositoryAdapter(OrcamentoJpaRepository jpaRepository, OrcamentoMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Orcamento salvar(Orcamento orcamento) {
        OrcamentoEntity entity = mapper.toEntity(orcamento);
        OrcamentoEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Orcamento> buscarPorId(UUID id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Orcamento> buscarPorOrdemServicoId(UUID ordemServicoId) {
        return jpaRepository.findByOrdemServicoId(ordemServicoId)
                .map(mapper::toDomain);
    }
}