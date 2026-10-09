package br.com.fiap.oficina_mecanica.ordemservico.infrastructure.orcamento;

import br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.usecase.NotificadorOrcamento;
import br.com.fiap.oficina_mecanica.ordemservico.domain.orcamento.Orcamento;
import br.com.fiap.oficina_mecanica.ordemservico.infrastructure.ordemservico.OrdemServicoJpaRepository;
import br.com.fiap.oficina_mecanica.ordemservico.infrastructure.ordemservico.entity.OrdemServicoEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class NotificadorEmailAdapter implements NotificadorOrcamento {

    private static final Logger LOG = LoggerFactory.getLogger(NotificadorEmailAdapter.class);
    private final OrdemServicoJpaRepository ordemServicoJpaRepository;

    public NotificadorEmailAdapter(OrdemServicoJpaRepository ordemServicoJpaRepository) {
        this.ordemServicoJpaRepository = ordemServicoJpaRepository;
    }

    @Override
    public void notificarOrcamentoGerado(Orcamento orcamento) {
        OrdemServicoEntity os = ordemServicoJpaRepository.findById(orcamento.getOrdemServicoId())
                .orElseThrow();

        LOG.info("\n=========================================================================" +
                        "\n📧 [SIMULAÇÃO DE E-MAIL] - NOVO ORÇAMENTO GERADO" +
                        "\n=========================================================================" +
                        "\nOlá! O diagnóstico do seu veículo foi concluído e o orçamento da OS {} está pronto." +
                        "\n" +
                        "\n🔗 Acesse o portal: http://localhost:8080/api/orcamentos/{}/cliente/visualizar" +
                        "\n🔑 Para sua segurança, utilize o seu CPF e o Código de Acesso abaixo:" +
                        "\n" +
                        "\n   Código de Acesso: {}" +
                        "\n" +
                        "\n=========================================================================\n",
                os.getId(), orcamento.getId(), os.getCodigoAcesso());
    }
}