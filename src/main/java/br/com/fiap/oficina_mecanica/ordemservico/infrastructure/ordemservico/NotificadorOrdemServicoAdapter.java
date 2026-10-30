package br.com.fiap.oficina_mecanica.ordemservico.infrastructure.ordemservico;

import br.com.fiap.oficina_mecanica.compartilhado.infrastructure.email.EnviadorEmail;
import br.com.fiap.oficina_mecanica.compartilhado.infrastructure.evento.ExecucaoSegura;
import br.com.fiap.oficina_mecanica.ordemservico.domain.evento.OrdemServicoAprovada;
import br.com.fiap.oficina_mecanica.ordemservico.domain.evento.OrdemServicoCancelada;
import br.com.fiap.oficina_mecanica.ordemservico.domain.evento.OrdemServicoCriada;
import br.com.fiap.oficina_mecanica.ordemservico.domain.evento.OrdemServicoFinalizada;
import br.com.fiap.oficina_mecanica.ordemservico.domain.evento.OrdemServicoRecusada;
import br.com.fiap.oficina_mecanica.veiculo.domain.VeiculoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.UUID;

@Component
public class NotificadorOrdemServicoAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(NotificadorOrdemServicoAdapter.class);

    private final VeiculoRepository veiculoRepository;
    private final EnviadorEmail enviadorEmail;
    private final ResolvedorEmailCliente resolvedorEmail;
    private final String urlBase;

    public NotificadorOrdemServicoAdapter(VeiculoRepository veiculoRepository,
                                          EnviadorEmail enviadorEmail,
                                          ResolvedorEmailCliente resolvedorEmail,
                                          @Value("${app.notificacao.url-base}") String urlBase) {
        this.veiculoRepository = veiculoRepository;
        this.enviadorEmail = enviadorEmail;
        this.resolvedorEmail = resolvedorEmail;
        this.urlBase = urlBase.endsWith("/") ? urlBase.substring(0, urlBase.length() - 1) : urlBase;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoCriar(OrdemServicoCriada evento) {
        ExecucaoSegura.executar(LOG, "o aviso de ordem recebida", () ->
                enviar(evento.ordemServicoId(), "Ordem de serviço recebida",
                        "Olá! Recebemos o seu veículo e a sua ordem de serviço foi aberta." +
                                acessoAoAndamento(evento.ordemServicoId(), evento.codigoAcesso())));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoAprovar(OrdemServicoAprovada evento) {
        ExecucaoSegura.executar(LOG, "o aviso de orçamento aprovado", () -> {
            String aprovacao = evento.aprovacaoParcial() ? "foi aprovado parcialmente" : "foi aprovado";

            enviar(evento.ordemServicoId(), "Orçamento aprovado",
                    "Olá! O orçamento " + aprovacao + " e o serviço no seu veículo será iniciado em breve." +
                            acessoAoAndamento(evento.ordemServicoId(), evento.codigoAcesso()));
        });
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoRecusar(OrdemServicoRecusada evento) {
        ExecucaoSegura.executar(LOG, "o aviso de orçamento não aprovado", () ->
                enviar(evento.ordemServicoId(), "Orçamento não aprovado",
                        "Olá! O orçamento não foi aprovado e a ordem de serviço foi encerrada sem execução." +
                                "\nEm caso de dúvidas, entre em contato com a oficina."));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoCancelar(OrdemServicoCancelada evento) {
        ExecucaoSegura.executar(LOG, "o aviso de ordem cancelada", () ->
                enviar(evento.ordemServicoId(), "Ordem de serviço cancelada",
                        "Olá! A sua ordem de serviço foi cancelada." +
                                "\nEm caso de dúvidas ou para combinar a retirada do veículo, entre em contato com a oficina."));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoFinalizar(OrdemServicoFinalizada evento) {
        ExecucaoSegura.executar(LOG, "o aviso de veículo pronto", () -> {
            String veiculo = veiculoRepository.buscarDetalhePorId(evento.veiculoId())
                    .map(v -> v.modelo() + " (placa " + v.placa() + ")")
                    .orElse("veículo");

            enviar(evento.ordemServicoId(), "Veículo pronto para retirada",
                    "Olá! O serviço foi finalizado e o seu " + veiculo + " está pronto para retirada.");
        });
    }

    private String acessoAoAndamento(UUID ordemServicoId, String codigoAcesso) {
        return "\n\n🔗 Acompanhe o andamento: " + urlBase + "/api/publico/ordens-servico/" + ordemServicoId + "/consultar" +
                "\n🔑 Para sua segurança, utilize o seu CPF e o Código de Acesso abaixo:" +
                "\n\n   Código de Acesso: " + codigoAcesso;
    }

    private void enviar(UUID ordemServicoId, String assunto, String corpo) {
        resolvedorEmail.porOrdemServico(ordemServicoId).ifPresentOrElse(
                email -> enviadorEmail.enviar(email, assunto, corpo),
                () -> LOG.warn("Cliente sem e-mail para a ordem de serviço {}; o aviso \"{}\" não foi enviado.",
                        ordemServicoId, assunto));
    }
}
