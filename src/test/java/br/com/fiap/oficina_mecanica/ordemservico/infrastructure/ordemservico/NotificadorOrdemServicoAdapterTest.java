package br.com.fiap.oficina_mecanica.ordemservico.infrastructure.ordemservico;

import br.com.fiap.oficina_mecanica.compartilhado.infrastructure.email.EnviadorEmail;
import br.com.fiap.oficina_mecanica.ordemservico.domain.evento.OrdemServicoAprovada;
import br.com.fiap.oficina_mecanica.ordemservico.domain.evento.OrdemServicoCancelada;
import br.com.fiap.oficina_mecanica.ordemservico.domain.evento.OrdemServicoCriada;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificadorOrdemServicoAdapterTest {

    private static final String URL_BASE = "http://oficina.test";
    private static final String EMAIL = "cliente@exemplo.com";

    @Mock
    private VeiculoRepository veiculoRepository;

    @Mock
    private EnviadorEmail enviadorEmail;

    @Mock
    private ResolvedorEmailCliente resolvedorEmail;

    private final ListAppender<ILoggingEvent> logs = new ListAppender<>();
    private final Logger logger = (Logger) LoggerFactory.getLogger(NotificadorOrdemServicoAdapter.class);

    private final UUID ordemId = UUID.randomUUID();
    private final UUID veiculoId = UUID.randomUUID();

    private NotificadorOrdemServicoAdapter adapter() {
        return new NotificadorOrdemServicoAdapter(veiculoRepository, enviadorEmail, resolvedorEmail, URL_BASE);
    }

    private void clienteComEmail() {
        when(resolvedorEmail.porOrdemServico(ordemId)).thenReturn(Optional.of(EMAIL));
    }

    private String[] emailEnviado() {
        ArgumentCaptor<String> assunto = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> corpo = ArgumentCaptor.forClass(String.class);
        verify(enviadorEmail).enviar(org.mockito.ArgumentMatchers.eq(EMAIL), assunto.capture(), corpo.capture());
        return new String[]{assunto.getValue(), corpo.getValue()};
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
        clienteComEmail();
        when(veiculoRepository.buscarDetalhePorId(veiculoId))
                .thenReturn(Optional.of(new VeiculoDetalhe(veiculoId, "ABC1D23", "Fiat", "Argo", 2022)));

        adapter().aoFinalizar(new OrdemServicoFinalizada(ordemId, veiculoId));

        String[] email = emailEnviado();
        assertThat(email[0]).isEqualTo("Veículo pronto para retirada");
        assertThat(email[1])
                .contains("O serviço foi finalizado")
                .contains("Argo (placa ABC1D23) está pronto para retirada");
    }

    @Test
    void semDadosDoVeiculoUsaTextoGenerico() {
        clienteComEmail();
        when(veiculoRepository.buscarDetalhePorId(veiculoId)).thenReturn(Optional.empty());

        adapter().aoFinalizar(new OrdemServicoFinalizada(ordemId, veiculoId));

        assertThat(emailEnviado()[1]).contains("o seu veículo está pronto para retirada");
    }

    @Test
    void avisoDeFinalizacaoNaoExpoeIdDaOrdem() {
        clienteComEmail();
        when(veiculoRepository.buscarDetalhePorId(veiculoId)).thenReturn(Optional.empty());

        adapter().aoFinalizar(new OrdemServicoFinalizada(ordemId, veiculoId));

        assertThat(emailEnviado()[1]).doesNotContain(ordemId.toString());
    }

    @Test
    void avisaQueAOrdemFoiRecebidaComLinkDeAcompanhamentoECodigoDeAcesso() {
        clienteComEmail();

        adapter().aoCriar(new OrdemServicoCriada(ordemId, "ABC123"));

        String[] email = emailEnviado();
        assertThat(email[0]).isEqualTo("Ordem de serviço recebida");
        assertThat(email[1])
                .contains("Acompanhe o andamento: " + URL_BASE + "/api/publico/ordens-servico/" + ordemId + "/consultar")
                .contains("CPF e o Código de Acesso")
                .contains("Código de Acesso: ABC123");
    }

    @Test
    void urlBaseComBarraNoFinalNaoDuplicaABarra() {
        clienteComEmail();

        new NotificadorOrdemServicoAdapter(veiculoRepository, enviadorEmail, resolvedorEmail, URL_BASE + "/")
                .aoCriar(new OrdemServicoCriada(ordemId, "ABC123"));

        assertThat(emailEnviado()[1])
                .contains(URL_BASE + "/api/publico/ordens-servico/" + ordemId + "/consultar")
                .doesNotContain("//api");
    }

    @Test
    void avisaAprovacaoTotalComLinkDeAcompanhamentoECodigoDeAcesso() {
        clienteComEmail();

        adapter().aoAprovar(new OrdemServicoAprovada(ordemId, "ABC123", false));

        String[] email = emailEnviado();
        assertThat(email[0]).isEqualTo("Orçamento aprovado");
        assertThat(email[1])
                .contains("O orçamento foi aprovado e o serviço")
                .contains("Acompanhe o andamento: " + URL_BASE + "/api/publico/ordens-servico/" + ordemId + "/consultar")
                .contains("CPF e o Código de Acesso")
                .contains("Código de Acesso: ABC123")
                .doesNotContain("parcialmente");
    }

    @Test
    void avisaAprovacaoParcial() {
        clienteComEmail();

        adapter().aoAprovar(new OrdemServicoAprovada(ordemId, "ABC123", true));

        assertThat(emailEnviado()[1])
                .contains("O orçamento foi aprovado parcialmente e o serviço")
                .contains("Código de Acesso: ABC123");
    }

    @Test
    void avisaQueOOrcamentoNaoFoiAprovadoENaoExpoeDadosDeAcesso() {
        clienteComEmail();

        adapter().aoRecusar(new OrdemServicoRecusada(ordemId));

        String[] email = emailEnviado();
        assertThat(email[0]).isEqualTo("Orçamento não aprovado");
        assertThat(email[1])
                .contains("encerrada sem execução")
                .doesNotContain(ordemId.toString());
    }

    @Test
    void avisaQueAOrdemFoiCanceladaENaoExpoeDadosDeAcesso() {
        clienteComEmail();

        adapter().aoCancelar(new OrdemServicoCancelada(ordemId));

        String[] email = emailEnviado();
        assertThat(email[0]).isEqualTo("Ordem de serviço cancelada");
        assertThat(email[1])
                .contains("A sua ordem de serviço foi cancelada")
                .doesNotContain(ordemId.toString());
    }

    @Test
    void clienteSemEmailNaoEnviaERegistraAviso() {
        when(resolvedorEmail.porOrdemServico(ordemId)).thenReturn(Optional.empty());

        adapter().aoCancelar(new OrdemServicoCancelada(ordemId));

        verify(enviadorEmail, never()).enviar(any(), any(), any());
        assertThat(logs.list).hasSize(1);
        assertThat(logs.list.getFirst().getLevel()).isEqualTo(Level.WARN);
    }

    @Test
    void falhaNoEnvioNaoPropagaERegistraErro() {
        clienteComEmail();
        org.mockito.Mockito.doThrow(new IllegalStateException("smtp fora do ar"))
                .when(enviadorEmail).enviar(any(), any(), any());

        assertThatCode(() -> adapter().aoCriar(new OrdemServicoCriada(ordemId, "ABC123")))
                .doesNotThrowAnyException();

        assertThat(logs.list).hasSize(1);
        assertThat(logs.list.getFirst().getLevel()).isEqualTo(Level.ERROR);
    }

    @Test
    void falhaAoBuscarOVeiculoNaoPropagaERegistraErro() {
        when(veiculoRepository.buscarDetalhePorId(veiculoId)).thenThrow(new IllegalStateException("banco indisponível"));

        assertThatCode(() -> adapter().aoFinalizar(new OrdemServicoFinalizada(ordemId, veiculoId)))
                .doesNotThrowAnyException();

        assertThat(logs.list).hasSize(1);
        assertThat(logs.list.getFirst().getLevel()).isEqualTo(Level.ERROR);
        verifyNoInteractions(enviadorEmail);
    }
}
