package br.com.fiap.oficina_mecanica.ordemservico.infrastructure.ordemservico;

import br.com.fiap.oficina_mecanica.compartilhado.domain.Pagina;
import br.com.fiap.oficina_mecanica.compartilhado.domain.Paginacao;
import br.com.fiap.oficina_mecanica.ordemservico.domain.orcamento.Orcamento;
import br.com.fiap.oficina_mecanica.ordemservico.domain.orcamento.OrcamentoRepository;
import br.com.fiap.oficina_mecanica.ordemservico.domain.ordemservico.OrdemServico;
import br.com.fiap.oficina_mecanica.ordemservico.domain.ordemservico.OrdemServicoRepository;
import br.com.fiap.oficina_mecanica.ordemservico.domain.ordemservico.StatusOrdemServico;
import br.com.fiap.oficina_mecanica.ordemservico.infrastructure.ordemservico.entity.OrdemServicoEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class OrdemServicoRepositoryAdapter implements OrdemServicoRepository {

    private final OrdemServicoJpaRepository jpaRepository;
    private final HistoricoStatusJpaRepository historicoRepository;
    private final OrdemServicoMapper mapper;
    private final OrcamentoRepository orcamentoRepository;

    public OrdemServicoRepositoryAdapter(OrdemServicoJpaRepository jpaRepository,
                                         HistoricoStatusJpaRepository historicoRepository,
                                         OrdemServicoMapper mapper,
                                         OrcamentoRepository orcamentoRepository) {
        this.jpaRepository = jpaRepository;
        this.historicoRepository = historicoRepository;
        this.mapper = mapper;
        this.orcamentoRepository = orcamentoRepository;
    }

    @Override
    public OrdemServico salvar(OrdemServico ordemServico) {
        OrdemServicoEntity savedEntity = jpaRepository.save(mapper.toEntity(ordemServico));
        registrarNovasTransicoes(ordemServico);
        return mapper.toDomain(savedEntity, ordemServico.getOrcamentos());
    }

    @Override
    public Optional<OrdemServico> buscarPorId(UUID id) {
        return jpaRepository.findById(id)
                .map(entity -> mapper.toDomain(entity, orcamentosDa(entity.getId())));
    }

    @Override
    public Pagina<OrdemServico> listar(StatusOrdemServico status, Paginacao paginacao) {
        // Desempate por id mantém a ordem estável entre páginas.
        Sort ordenacao = Sort.by(Sort.Direction.DESC, "dataAbertura").and(Sort.by("id"));
        Pageable pageable = PageRequest.of(paginacao.pagina(), paginacao.tamanho(), ordenacao);

        Page<OrdemServicoEntity> pagina = status == null
                ? jpaRepository.findAll(pageable)
                : jpaRepository.findByStatus(status, pageable);

        // Listagem é um resumo: orçamentos não são carregados para evitar N+1 consultas.
        List<OrdemServico> ordens = pagina.getContent().stream()
                .map(entity -> mapper.toDomain(entity, List.of()))
                .toList();
        return Pagina.de(ordens, paginacao, pagina.getTotalElements());
    }

    // O id da transição é definido no domínio, então salvar a mesma OS duas vezes não duplica o registro.
    private void registrarNovasTransicoes(OrdemServico ordemServico) {
        historicoRepository.saveAll(ordemServico.getNovasTransicoes().stream()
                .map(transicao -> mapper.toEntity(ordemServico.getId(), transicao))
                .toList());
    }

    private List<Orcamento> orcamentosDa(UUID ordemServicoId) {
        return orcamentoRepository.buscarPorOrdemServicoId(ordemServicoId);
    }
}
