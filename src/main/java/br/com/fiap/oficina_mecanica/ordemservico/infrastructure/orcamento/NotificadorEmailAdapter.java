package br.com.fiap.oficina_mecanica.ordemservico.infrastructure.orcamento;

import br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.usecase.NotificadorOrcamento;
import br.com.fiap.oficina_mecanica.ordemservico.domain.orcamento.Orcamento;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class NotificadorEmailAdapter implements NotificadorOrcamento {

    private static final Logger LOG = LoggerFactory.getLogger(NotificadorEmailAdapter.class);

    @Override
    public void notificarOrcamentoGerado(Orcamento orcamento) {

        LOG.info("Simulando envio de e-mail para aprovação do orçamento: {}", orcamento.getId());
    }
}