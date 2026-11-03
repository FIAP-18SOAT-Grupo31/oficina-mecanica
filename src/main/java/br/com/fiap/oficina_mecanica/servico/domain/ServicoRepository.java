package br.com.fiap.oficina_mecanica.servico.domain;

import br.com.fiap.oficina_mecanica.compartilhado.domain.Pagina;

import java.util.Optional;
import java.util.UUID;

public interface ServicoRepository {
    Servico salvar(Servico servico);
    Optional<Servico> buscarPorId(UUID id);
    Pagina<Servico> listar(String nome, int pagina, int tamanho);
    boolean existePorId(UUID id);
    boolean existePorNome(String nome);
    boolean existeOutroComNome(String nome, UUID idIgnorado);
    void excluirPorId(UUID id);
}
