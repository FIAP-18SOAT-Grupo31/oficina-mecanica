package br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.usecase;

import br.com.fiap.oficina_mecanica.ordemservico.domain.ordemservico.OrdemServico;

public interface NotificadorOrdemServico {
    void notificarOrdemAprovada(OrdemServico ordemServico, boolean aprovacaoParcial);
    void notificarOrdemRecusada(OrdemServico ordemServico);
    void notificarOrdemCancelada(OrdemServico ordemServico);
    void notificarOrdemFinalizada(OrdemServico ordemServico);
}
