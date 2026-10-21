package br.com.fiap.oficina_mecanica.cliente.domain;

import br.com.fiap.oficina_mecanica.compartilhado.domain.Pagina;

import java.util.Optional;
import java.util.UUID;

public interface ClienteRepository {
    Cliente salvar(Cliente cliente);
    Optional<Cliente> buscarPorId(UUID id);
    Pagina<Cliente> listar(String nome, String documento, int pagina, int tamanho);
    boolean existePorId(UUID id);
    boolean existePorDocumento(String documento);
    boolean existeOutroComDocumento(String documento, UUID idIgnorado);
    void excluirPorId(UUID id);
    Optional<String> buscarDocumentoPorId(UUID id);
}
