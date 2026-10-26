package br.com.fiap.oficina_mecanica.ordemservico.infrastructure.orcamento;

import br.com.fiap.oficina_mecanica.compartilhado.infrastructure.evento.ExecucaoSegura;
import br.com.fiap.oficina_mecanica.ordemservico.domain.evento.OrcamentoGerado;
import br.com.fiap.oficina_mecanica.ordemservico.infrastructure.ordemservico.OrdemServicoJpaRepository;
import br.com.fiap.oficina_mecanica.ordemservico.infrastructure.ordemservico.entity.OrdemServicoEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class NotificadorPortalClienteAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(NotificadorPortalClienteAdapter.class);
    private final OrdemServicoJpaRepository ordemServicoJpaRepository;

    public NotificadorPortalClienteAdapter(OrdemServicoJpaRepository ordemServicoJpaRepository) {
        this.ordemServicoJpaRepository = ordemServicoJpaRepository;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoGerarOrcamento(OrcamentoGerado evento) {
        ExecucaoSegura.executar(LOG, "o aviso do portal do cliente", () -> notificar(evento));
    }

    private void notificar(OrcamentoGerado evento) {
        OrdemServicoEntity os = ordemServicoJpaRepository.findById(evento.ordemServicoId())
                .orElseThrow();

        LOG.info("\n=========================================================================" +
                        "\n📧 [SIMULAÇÃO DE E-MAIL] - NOVO ORÇAMENTO GERADO" +
                        "\n=========================================================================" +
                        "\nOlá! O diagnóstico do seu veículo foi concluído e o orçamento da sua ordem de serviço está pronto." +
                        "\n" +
                        "\n🔗 Acesse o portal: http://localhost:8080/api/publico/ordens-servico/{}/consulta" +
                        "\n🔑 Para sua segurança, utilize o seu CPF e o Código de Acesso abaixo:" +
                        "\n" +
                        "\n   Código de Acesso: {}" +
                        "\n" +
                        "\n=========================================================================\n",
                os.getId(), os.getCodigoAcesso());
    }
}
