package br.com.fiap.oficina_mecanica.veiculo.infrastructure;

import br.com.fiap.oficina_mecanica.compartilhado.domain.Pagina;
import br.com.fiap.oficina_mecanica.compartilhado.infrastructure.persistence.TextoLike;
import br.com.fiap.oficina_mecanica.veiculo.domain.Veiculo;
import br.com.fiap.oficina_mecanica.veiculo.domain.VeiculoDetalhe;
import br.com.fiap.oficina_mecanica.veiculo.domain.VeiculoRepository;
import br.com.fiap.oficina_mecanica.veiculo.domain.VeiculoResumo;
import br.com.fiap.oficina_mecanica.veiculo.infrastructure.entity.VeiculoEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class VeiculoRepositoryAdapter implements VeiculoRepository {

    private final VeiculoJpaRepository jpaRepository;

    public VeiculoRepositoryAdapter(VeiculoJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Veiculo salvar(Veiculo veiculo) {
        return VeiculoMapper.toDomain(jpaRepository.save(VeiculoMapper.toEntity(veiculo)));
    }

    @Override
    public Optional<Veiculo> buscarPorId(UUID id) {
        return jpaRepository.findById(id).map(VeiculoMapper::toDomain);
    }

    @Override
    public Pagina<Veiculo> listar(UUID clienteId, String placa, String modelo, int pagina, int tamanho) {
        PageRequest pageRequest = PageRequest.of(pagina, tamanho, Sort.by("placa", "id"));
        Page<VeiculoEntity> resultado = jpaRepository.findAll(filtro(clienteId, placa, modelo), pageRequest);

        List<Veiculo> veiculos = resultado.getContent().stream().map(VeiculoMapper::toDomain).toList();
        return new Pagina<>(veiculos, pagina, tamanho, resultado.getTotalElements());
    }

    @Override
    public boolean existePorId(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public List<VeiculoResumo> buscarResumosPorIds(Collection<UUID> ids) {
        return jpaRepository.findAllById(ids).stream()
                .map(veiculo -> new VeiculoResumo(veiculo.getId(), veiculo.getPlaca(), veiculo.getMarca(), veiculo.getModelo()))
                .toList();
    }

    @Override
    public Optional<VeiculoDetalhe> buscarDetalhePorId(UUID id) {
        return jpaRepository.findById(id)
                .map(v -> new VeiculoDetalhe(v.getId(), v.getPlaca(), v.getMarca(), v.getModelo(), v.getAno()));
    }

    @Override
    public boolean existePorClienteId(UUID clienteId) {
        return jpaRepository.existsByClienteId(clienteId);
    }

    @Override
    public boolean existePorPlaca(String placa) {
        return jpaRepository.existsByPlaca(placa);
    }

    @Override
    public boolean existeOutroComPlaca(String placa, UUID idIgnorado) {
        return jpaRepository.existsByPlacaAndIdNot(placa, idIgnorado);
    }

    @Override
    public void excluirPorId(UUID id) {
        jpaRepository.deleteById(id);
    }

    private static Specification<VeiculoEntity> filtro(UUID clienteId, String placa, String modelo) {
        return (root, query, builder) -> {
            List<Predicate> predicados = new ArrayList<>();

            if (clienteId != null) {
                predicados.add(builder.equal(root.get("clienteId"), clienteId));
            }

            if (placa != null) {
                predicados.add(builder.equal(root.get("placa"), placa));
            }

            if (modelo != null) {
                predicados.add(builder.like(builder.lower(root.<String>get("modelo")), TextoLike.contem(modelo),
                        TextoLike.ESCAPE));
            }

            return builder.and(predicados.toArray(new Predicate[0]));
        };
    }
}
