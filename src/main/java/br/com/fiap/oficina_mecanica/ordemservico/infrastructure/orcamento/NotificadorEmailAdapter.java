package br.com.fiap.oficina_mecanica.ordemservico.infrastructure.orcamento;

import br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.usecase.NotificadorOrcamento;
import br.com.fiap.oficina_mecanica.ordemservico.domain.orcamento.Orcamento;
import org.springframework.stereotype.Component;

@Component
public class NotificadorEmailAdapter implements NotificadorOrcamento {

    @Override
    public void notificarOrcamentoGerado(Orcamento orcamento) {

        System.out.println("Simulando envio de e-mail para aprovação do orçamento: " + orcamento.getId());
    }
}