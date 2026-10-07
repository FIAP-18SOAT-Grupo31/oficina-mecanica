package br.com.fiap.oficina_mecanica.ordemservico.infrastructure.ordemservico;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import br.com.fiap.oficina_mecanica.ordemservico.domain.ordemservico.OrdemServico;
import br.com.fiap.oficina_mecanica.ordemservico.domain.ordemservico.OrdemServicoRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor 
public class OrdemServicoRepositoryAdapter implements OrdemServicoRepository {

    private final OrdemServicoJpaRepository ordemServicoJpaRepository;
    private final OrdemServicoMapper ordemServicoMapper;

    @Override
    public Optional<OrdemServico> findById(UUID id) {
        return ordemServicoJpaRepository.findById(id)
                .map(os -> ordemServicoMapper.toDomain(os));
    }

    @Override
    public OrdemServico save(OrdemServico ordemServico) {
        var ordemServicoEntity = ordemServicoMapper.toEntity(ordemServico);
        var savedEntity = ordemServicoJpaRepository.save(ordemServicoEntity);
        return ordemServicoMapper.toDomain(savedEntity);
    }
}
