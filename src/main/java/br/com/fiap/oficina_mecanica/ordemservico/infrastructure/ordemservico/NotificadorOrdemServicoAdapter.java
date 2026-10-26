package br.com.fiap.oficina_mecanica.ordemservico.infrastructure.ordemservico;

import br.com.fiap.oficina_mecanica.compartilhado.infrastructure.evento.ExecucaoSegura;
import br.com.fiap.oficina_mecanica.ordemservico.domain.evento.OrdemServicoAprovada;
import br.com.fiap.oficina_mecanica.ordemservico.domain.evento.OrdemServicoCancelada;
import br.com.fiap.oficina_mecanica.ordemservico.domain.evento.OrdemServicoFinalizada;
import br.com.fiap.oficina_mecanica.ordemservico.domain.evento.OrdemServicoRecusada;
import br.com.fiap.oficina_mecanica.veiculo.domain.VeiculoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class NotificadorOrdemServicoAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(NotificadorOrdemServicoAdapter.class);

    private final VeiculoRepository veiculoRepository;

    public NotificadorOrdemServicoAdapter(VeiculoRepository veiculoRepository) {
        this.veiculoRepository = veiculoRepository;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoAprovar(OrdemServicoAprovada evento) {
        ExecucaoSegura.executar(LOG, "o aviso de orçamento aprovado", () -> {
            String aprovacao = evento.aprovacaoParcial() ? "foi aprovado parcialmente" : "foi aprovado";

            LOG.info("\n=========================================================================" +
                            "\n📧 [SIMULAÇÃO DE E-MAIL] - ORÇAMENTO APROVADO" +
                            "\n=========================================================================" +
                            "\nOlá! O orçamento {} e o serviço no seu veículo será iniciado em breve." +
                            "\n" +
                            "\n🔗 Acompanhe o andamento: http://localhost:8080/api/publico/ordens-servico/{}/consulta" +
                            "\n🔑 Para sua segurança, utilize o seu CPF e o Código de Acesso abaixo:" +
                            "\n" +
                            "\n   Código de Acesso: {}" +
                            "\n" +
                            "\n=========================================================================\n",
                    aprovacao, evento.ordemServicoId(), evento.codigoAcesso());
        });
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoRecusar(OrdemServicoRecusada evento) {
        ExecucaoSegura.executar(LOG, "o aviso de orçamento não aprovado", () ->
                LOG.info("\n=========================================================================" +
                        "\n📧 [SIMULAÇÃO DE E-MAIL] - ORÇAMENTO NÃO APROVADO" +
                        "\n=========================================================================" +
                        "\nOlá! O orçamento não foi aprovado e a ordem de serviço foi encerrada sem execução." +
                        "\nEm caso de dúvidas, entre em contato com a oficina." +
                        "\n" +
                        "\n=========================================================================\n"));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoCancelar(OrdemServicoCancelada evento) {
        ExecucaoSegura.executar(LOG, "o aviso de ordem cancelada", () ->
                LOG.info("\n=========================================================================" +
                        "\n📧 [SIMULAÇÃO DE E-MAIL] - ORDEM DE SERVIÇO CANCELADA" +
                        "\n=========================================================================" +
                        "\nOlá! A sua ordem de serviço foi cancelada." +
                        "\nEm caso de dúvidas ou para combinar a retirada do veículo, entre em contato com a oficina." +
                        "\n" +
                        "\n=========================================================================\n"));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoFinalizar(OrdemServicoFinalizada evento) {
        ExecucaoSegura.executar(LOG, "o aviso de veículo pronto", () -> {
            String veiculo = veiculoRepository.buscarDetalhePorId(evento.veiculoId())
                    .map(v -> v.modelo() + " (placa " + v.placa() + ")")
                    .orElse("veículo");

            LOG.info("\n=========================================================================" +
                            "\n📧 [SIMULAÇÃO DE E-MAIL] - VEÍCULO PRONTO PARA RETIRADA" +
                            "\n=========================================================================" +
                            "\nOlá! O serviço foi finalizado e o seu {} está pronto para retirada." +
                            "\n" +
                            "\n=========================================================================\n",
                    veiculo);
        });
    }
}
