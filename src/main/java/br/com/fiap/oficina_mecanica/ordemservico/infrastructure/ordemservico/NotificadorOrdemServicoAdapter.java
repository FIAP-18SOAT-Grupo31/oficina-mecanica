package br.com.fiap.oficina_mecanica.ordemservico.infrastructure.ordemservico;

import br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.usecase.NotificadorOrdemServico;
import br.com.fiap.oficina_mecanica.ordemservico.domain.ordemservico.OrdemServico;
import br.com.fiap.oficina_mecanica.veiculo.domain.VeiculoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class NotificadorOrdemServicoAdapter implements NotificadorOrdemServico {

    private static final Logger LOG = LoggerFactory.getLogger(NotificadorOrdemServicoAdapter.class);

    private final VeiculoRepository veiculoRepository;

    public NotificadorOrdemServicoAdapter(VeiculoRepository veiculoRepository) {
        this.veiculoRepository = veiculoRepository;
    }

    @Override
    public void notificarOrdemAprovada(OrdemServico ordemServico, boolean aprovacaoParcial) {
        String aprovacao = aprovacaoParcial ? "foi aprovado parcialmente" : "foi aprovado";

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
                aprovacao, ordemServico.getId(), ordemServico.getCodigoAcesso());
    }

    @Override
    public void notificarOrdemRecusada(OrdemServico ordemServico) {
        LOG.info("\n=========================================================================" +
                "\n📧 [SIMULAÇÃO DE E-MAIL] - ORÇAMENTO NÃO APROVADO" +
                "\n=========================================================================" +
                "\nOlá! O orçamento não foi aprovado e a ordem de serviço foi encerrada sem execução." +
                "\nEm caso de dúvidas, entre em contato com a oficina." +
                "\n" +
                "\n=========================================================================\n");
    }

    @Override
    public void notificarOrdemCancelada(OrdemServico ordemServico) {
        LOG.info("\n=========================================================================" +
                "\n📧 [SIMULAÇÃO DE E-MAIL] - ORDEM DE SERVIÇO CANCELADA" +
                "\n=========================================================================" +
                "\nOlá! A sua ordem de serviço foi cancelada." +
                "\nEm caso de dúvidas ou para combinar a retirada do veículo, entre em contato com a oficina." +
                "\n" +
                "\n=========================================================================\n");
    }

    @Override
    public void notificarOrdemFinalizada(OrdemServico ordemServico) {
        String veiculo = veiculoRepository.buscarDetalhePorId(ordemServico.getVeiculoId())
                .map(v -> v.modelo() + " (placa " + v.placa() + ")")
                .orElse("veículo");

        LOG.info("\n=========================================================================" +
                        "\n📧 [SIMULAÇÃO DE E-MAIL] - VEÍCULO PRONTO PARA RETIRADA" +
                        "\n=========================================================================" +
                        "\nOlá! O serviço foi finalizado e o seu {} está pronto para retirada." +
                        "\n" +
                        "\n=========================================================================\n",
                veiculo);
    }
}
