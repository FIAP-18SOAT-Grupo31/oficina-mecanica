package br.com.fiap.oficina_mecanica.veiculo.domain;

import br.com.fiap.oficina_mecanica.compartilhado.domain.Pagina;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VeiculoRepository {
    Veiculo salvar(Veiculo veiculo);
    Optional<Veiculo> buscarPorId(UUID id);
    Pagina<Veiculo> listar(UUID clienteId, String placa, String modelo, int pagina, int tamanho);
    boolean existePorId(UUID id);
    List<VeiculoResumo> buscarResumosPorIds(Collection<UUID> ids);
    Optional<VeiculoDetalhe> buscarDetalhePorId(UUID id);
    boolean existePorClienteId(UUID clienteId);
    boolean existePorPlaca(String placa);
    boolean existeOutroComPlaca(String placa, UUID idIgnorado);
    void excluirPorId(UUID id);
}
