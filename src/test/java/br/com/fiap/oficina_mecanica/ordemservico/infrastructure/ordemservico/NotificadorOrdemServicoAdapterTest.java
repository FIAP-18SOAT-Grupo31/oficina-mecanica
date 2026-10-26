package br.com.fiap.oficina_mecanica.ordemservico.infrastructure.ordemservico;

import br.com.fiap.oficina_mecanica.ordemservico.domain.evento.OrdemServicoAprovada;
import br.com.fiap.oficina_mecanica.ordemservico.domain.evento.OrdemServicoCancelada;
import br.com.fiap.oficina_mecanica.ordemservico.domain.evento.OrdemServicoFinalizada;
import br.com.fiap.oficina_mecanica.ordemservico.domain.evento.OrdemServicoRecusada;
import br.com.fiap.oficina_mecanica.veiculo.domain.VeiculoDetalhe;
import br.com.fiap.oficina_mecanica.veiculo.domain.VeiculoRepository;
import ch.qos.logback.classic.Level;
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

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificadorOrdemServicoAdapterTest {

    @Mock
    private VeiculoRepository veiculoRepository;

    private final ListAppender<ILoggingEvent> logs = new ListAppender<>();
    private final Logger logger = (Logger) LoggerFactory.getLogger(NotificadorOrdemServicoAdapter.class);

    private final UUID ordemId = UUID.randomUUID();
    private final UUID veiculoId = UUID.randomUUID();

    private NotificadorOrdemServicoAdapter adapter() {
        return new NotificadorOrdemServicoAdapter(veiculoRepository);
    }

    private String mensagem() {
        assertThat(logs.list).hasSize(1);
        return logs.list.getFirst().getFormattedMessage();
    }

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

        adapter().aoFinalizar(new OrdemServicoFinalizada(ordemId, veiculoId));

        assertThat(mensagem())
                .contains("VEÍCULO PRONTO PARA RETIRADA")
                .contains("O serviço foi finalizado")
                .contains("Argo (placa ABC1D23) está pronto para retirada");
    }

    @Test
    void semDadosDoVeiculoUsaTextoGenerico() {
        when(veiculoRepository.buscarDetalhePorId(veiculoId)).thenReturn(Optional.empty());

        adapter().aoFinalizar(new OrdemServicoFinalizada(ordemId, veiculoId));

        assertThat(mensagem()).contains("o seu veículo está pronto para retirada");
    }

    @Test
    void avisoDeFinalizacaoNaoExpoeIdDaOrdem() {
        when(veiculoRepository.buscarDetalhePorId(veiculoId)).thenReturn(Optional.empty());

        adapter().aoFinalizar(new OrdemServicoFinalizada(ordemId, veiculoId));

        assertThat(mensagem()).doesNotContain(ordemId.toString());
    }

    @Test
    void avisaAprovacaoTotalComLinkDeAcompanhamentoECodigoDeAcesso() {
        adapter().aoAprovar(new OrdemServicoAprovada(ordemId, "ABC123", false));

        assertThat(mensagem())
                .contains("ORÇAMENTO APROVADO")
                .contains("O orçamento foi aprovado e o serviço")
                .contains("Acompanhe o andamento: http://localhost:8080/api/publico/ordens-servico/" + ordemId + "/consulta")
                .contains("CPF e o Código de Acesso")
                .contains("Código de Acesso: ABC123")
                .doesNotContain("parcialmente");
    }

    @Test
    void avisaAprovacaoParcial() {
        adapter().aoAprovar(new OrdemServicoAprovada(ordemId, "ABC123", true));

        assertThat(mensagem())
                .contains("O orçamento foi aprovado parcialmente e o serviço")
                .contains("Código de Acesso: ABC123");
    }

    @Test
    void avisaQueOOrcamentoNaoFoiAprovadoENaoExpoeDadosDeAcesso() {
        adapter().aoRecusar(new OrdemServicoRecusada(ordemId));

        assertThat(mensagem())
                .contains("ORÇAMENTO NÃO APROVADO")
                .contains("encerrada sem execução")
                .doesNotContain(ordemId.toString());
    }

    @Test
    void avisaQueAOrdemFoiCanceladaENaoExpoeDadosDeAcesso() {
        adapter().aoCancelar(new OrdemServicoCancelada(ordemId));

        assertThat(mensagem())
                .contains("ORDEM DE SERVIÇO CANCELADA")
                .contains("A sua ordem de serviço foi cancelada")
                .doesNotContain(ordemId.toString());
    }

    @Test
    void falhaAoBuscarOVeiculoNaoPropagaERegistraErro() {
        when(veiculoRepository.buscarDetalhePorId(veiculoId)).thenThrow(new IllegalStateException("banco indisponível"));

        assertThatCode(() -> adapter().aoFinalizar(new OrdemServicoFinalizada(ordemId, veiculoId)))
                .doesNotThrowAnyException();

        assertThat(logs.list).hasSize(1);
        assertThat(logs.list.getFirst().getLevel()).isEqualTo(Level.ERROR);
    }
}
