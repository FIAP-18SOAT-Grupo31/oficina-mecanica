package br.com.fiap.oficina_mecanica.ordemservico.domain.ordemservico;

import br.com.fiap.oficina_mecanica.compartilhado.domain.Pagina;
import br.com.fiap.oficina_mecanica.compartilhado.domain.Paginacao;

import java.util.Optional;
import java.util.UUID;

public interface OrdemServicoRepository {
    OrdemServico salvar(OrdemServico ordemServico);
    Optional<OrdemServico> buscarPorId(UUID id);
    Pagina<OrdemServico> listar(StatusOrdemServico status, Paginacao paginacao);
}
