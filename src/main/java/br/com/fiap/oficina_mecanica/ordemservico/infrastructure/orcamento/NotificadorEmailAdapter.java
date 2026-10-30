package br.com.fiap.oficina_mecanica.ordemservico.infrastructure.orcamento;

import br.com.fiap.oficina_mecanica.compartilhado.infrastructure.email.EnviadorEmail;
import br.com.fiap.oficina_mecanica.compartilhado.infrastructure.evento.ExecucaoSegura;
import br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.dto.ClienteDetalheOutput;
import br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.usecase.ConsultorClientes;
import br.com.fiap.oficina_mecanica.ordemservico.domain.evento.OrcamentoGerado;
import br.com.fiap.oficina_mecanica.ordemservico.infrastructure.ordemservico.OrdemServicoJpaRepository;
import br.com.fiap.oficina_mecanica.ordemservico.infrastructure.ordemservico.entity.OrdemServicoEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class NotificadorEmailAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(NotificadorEmailAdapter.class);

    private final OrdemServicoJpaRepository ordemServicoJpaRepository;
    private final ConsultorClientes consultorClientes;
    private final EnviadorEmail enviadorEmail;
    private final String urlBase;

    public NotificadorEmailAdapter(OrdemServicoJpaRepository ordemServicoJpaRepository,
                                   ConsultorClientes consultorClientes,
                                   EnviadorEmail enviadorEmail,
                                   @Value("${app.notificacao.url-base}") String urlBase) {
        this.ordemServicoJpaRepository = ordemServicoJpaRepository;
        this.consultorClientes = consultorClientes;
        this.enviadorEmail = enviadorEmail;
        this.urlBase = urlBase.endsWith("/") ? urlBase.substring(0, urlBase.length() - 1) : urlBase;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoGerarOrcamento(OrcamentoGerado evento) {
        ExecucaoSegura.executar(LOG, "o aviso de orçamento gerado", () -> notificar(evento));
    }

    private void notificar(OrcamentoGerado evento) {
        OrdemServicoEntity os = ordemServicoJpaRepository.findById(evento.ordemServicoId())
                .orElseThrow();

        String email = consultorClientes.detalharPorId(os.getClienteId())
                .map(ClienteDetalheOutput::email)
                .orElse(null);
        if (email == null) {
            LOG.warn("Cliente sem e-mail para a ordem de serviço {}; o aviso de orçamento gerado não foi enviado.",
                    os.getId());
            return;
        }

        enviadorEmail.enviar(email, "Novo orçamento gerado",
                "Olá! O diagnóstico do seu veículo foi concluído e o orçamento da OS " + os.getId() + " está pronto." +
                        "\n\n🔗 Acesse o portal: " + urlBase + "/api/publico/orcamentos/" + evento.orcamentoId() + "/consultar" +
                        "\n🔑 Para sua segurança, utilize o seu CPF e o Código de Acesso abaixo:" +
                        "\n\n   Código de Acesso: " + os.getCodigoAcesso());
    }
}
