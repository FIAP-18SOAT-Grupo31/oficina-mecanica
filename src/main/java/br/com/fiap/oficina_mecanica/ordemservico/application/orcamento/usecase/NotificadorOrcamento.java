package br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.usecase;

import br.com.fiap.oficina_mecanica.ordemservico.domain.orcamento.Orcamento;

public interface NotificadorOrcamento {
    void notificarOrcamentoGerado(Orcamento orcamento);
}