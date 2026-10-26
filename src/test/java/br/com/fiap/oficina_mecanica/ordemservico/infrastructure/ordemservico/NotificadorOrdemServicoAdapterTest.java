package br.com.fiap.oficina_mecanica.ordemservico.infrastructure.ordemservico;

import br.com.fiap.oficina_mecanica.compartilhado.domain.Horario;
import br.com.fiap.oficina_mecanica.ordemservico.domain.ordemservico.OrdemServico;
import br.com.fiap.oficina_mecanica.ordemservico.domain.ordemservico.StatusOrdemServico;
import br.com.fiap.oficina_mecanica.veiculo.domain.VeiculoDetalhe;
import br.com.fiap.oficina_mecanica.veiculo.domain.VeiculoRepository;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificadorOrdemServicoAdapterTest {

    @Mock
    private VeiculoRepository veiculoRepository;

    private final ListAppender<ILoggingEvent> logs = new ListAppender<>();
    private final Logger logger = (Logger) LoggerFactory.getLogger(NotificadorOrdemServicoAdapter.class);

    private final UUID veiculoId = UUID.randomUUID();
    private final OrdemServico ordem = OrdemServico.builder()
            .id(UUID.randomUUID()).clienteId(UUID.randomUUID()).veiculoId(veiculoId)
            .relatoProblema("Barulho na suspensão dianteira").status(StatusOrdemServico.FINALIZADA)
            .dataAbertura(Horario.agora()).dataConclusao(Horario.agora()).codigoAcesso("ABC123")
            .orcamentos(new ArrayList<>())
            .build();

    @BeforeEach
    void anexarAppender() {
        logs.start();
        logger.addAppender(logs);
    }

    @AfterEach
    void removerAppender() {
        logger.detachAppender(logs);
    }

    @Test
    void avisaQueOVeiculoEstaProntoParaRetiradaComModeloEPlaca() {
        when(veiculoRepository.buscarDetalhePorId(veiculoId))
                .thenReturn(Optional.of(new VeiculoDetalhe(veiculoId, "ABC1D23", "Fiat", "Argo", 2022)));

        new NotificadorOrdemServicoAdapter(veiculoRepository).notificarOrdemFinalizada(ordem);

        assertThat(logs.list).hasSize(1);
        assertThat(logs.list.getFirst().getFormattedMessage())
                .contains("VEÍCULO PRONTO PARA RETIRADA")
                .contains("O serviço foi finalizado")
                .contains("Argo (placa ABC1D23) está pronto para retirada");
    }

    @Test
    void semDadosDoVeiculoUsaTextoGenerico() {
        when(veiculoRepository.buscarDetalhePorId(veiculoId)).thenReturn(Optional.empty());

        new NotificadorOrdemServicoAdapter(veiculoRepository).notificarOrdemFinalizada(ordem);

        assertThat(logs.list.getFirst().getFormattedMessage()).contains("o seu veículo está pronto para retirada");
    }

    @Test
    void naoExpoeCodigoDeAcessoNemIdDaOrdem() {
        when(veiculoRepository.buscarDetalhePorId(veiculoId)).thenReturn(Optional.empty());

        new NotificadorOrdemServicoAdapter(veiculoRepository).notificarOrdemFinalizada(ordem);

        assertThat(logs.list.getFirst().getFormattedMessage())
                .doesNotContain("ABC123")
                .doesNotContain(ordem.getId().toString());
    }

    @Test
    void avisaAprovacaoTotalComLinkDeAcompanhamentoECodigoDeAcesso() {
        new NotificadorOrdemServicoAdapter(veiculoRepository).notificarOrdemAprovada(ordem, false);

        assertThat(logs.list).hasSize(1);
        assertThat(logs.list.getFirst().getFormattedMessage())
                .contains("ORÇAMENTO APROVADO")
                .contains("O orçamento foi aprovado e o serviço")
                .contains("Acompanhe o andamento: http://localhost:8080/api/publico/ordens-servico/" + ordem.getId() + "/consulta")
                .contains("CPF e o Código de Acesso")
                .contains("Código de Acesso: ABC123")
                .doesNotContain("parcialmente");
    }

    @Test
    void avisaAprovacaoParcial() {
        new NotificadorOrdemServicoAdapter(veiculoRepository).notificarOrdemAprovada(ordem, true);

        assertThat(logs.list.getFirst().getFormattedMessage())
                .contains("O orçamento foi aprovado parcialmente e o serviço")
                .contains("Código de Acesso: ABC123");
    }

    @Test
    void avisaQueOOrcamentoNaoFoiAprovadoENaoExpoeDadosDeAcesso() {
        new NotificadorOrdemServicoAdapter(veiculoRepository).notificarOrdemRecusada(ordem);

        assertThat(logs.list).hasSize(1);
        assertThat(logs.list.getFirst().getFormattedMessage())
                .contains("ORÇAMENTO NÃO APROVADO")
                .contains("encerrada sem execução")
                .doesNotContain("ABC123")
                .doesNotContain(ordem.getId().toString());
    }

    @Test
    void avisaQueAOrdemFoiCanceladaENaoExpoeDadosDeAcesso() {
        new NotificadorOrdemServicoAdapter(veiculoRepository).notificarOrdemCancelada(ordem);

        assertThat(logs.list).hasSize(1);
        assertThat(logs.list.getFirst().getFormattedMessage())
                .contains("ORDEM DE SERVIÇO CANCELADA")
                .contains("A sua ordem de serviço foi cancelada")
                .doesNotContain("ABC123")
                .doesNotContain(ordem.getId().toString());
    }
}
