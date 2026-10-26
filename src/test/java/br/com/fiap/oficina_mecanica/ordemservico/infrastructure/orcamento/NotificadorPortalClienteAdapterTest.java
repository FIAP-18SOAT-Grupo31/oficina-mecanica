package br.com.fiap.oficina_mecanica.ordemservico.infrastructure.orcamento;

import br.com.fiap.oficina_mecanica.compartilhado.domain.Horario;
import br.com.fiap.oficina_mecanica.ordemservico.domain.evento.OrcamentoGerado;
import br.com.fiap.oficina_mecanica.ordemservico.domain.ordemservico.StatusOrdemServico;
import br.com.fiap.oficina_mecanica.ordemservico.infrastructure.ordemservico.OrdemServicoJpaRepository;
import br.com.fiap.oficina_mecanica.ordemservico.infrastructure.ordemservico.entity.OrdemServicoEntity;
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
class NotificadorPortalClienteAdapterTest {

    @Mock
    private OrdemServicoJpaRepository ordemServicoJpaRepository;

    private final ListAppender<ILoggingEvent> logs = new ListAppender<>();
    private final Logger logger = (Logger) LoggerFactory.getLogger(NotificadorPortalClienteAdapter.class);

    private final UUID ordemId = UUID.randomUUID();
    private final OrcamentoGerado evento = new OrcamentoGerado(UUID.randomUUID(), ordemId);

    private OrdemServicoEntity ordemEntity() {
        return new OrdemServicoEntity(ordemId, UUID.randomUUID(), UUID.randomUUID(), "Barulho na suspensão",
                StatusOrdemServico.AGUARDANDO_APROVACAO, Horario.agora(), null, "ABC123");
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
    void mensagemTrazLinkDoPortalComIdDaOrdemECodigoDeAcesso() {
        when(ordemServicoJpaRepository.findById(ordemId)).thenReturn(Optional.of(ordemEntity()));

        new NotificadorPortalClienteAdapter(ordemServicoJpaRepository).aoGerarOrcamento(evento);

        assertThat(logs.list).hasSize(1);
        assertThat(logs.list.getFirst().getFormattedMessage())
                .contains("NOVO ORÇAMENTO GERADO")
                .contains("orçamento da sua ordem de serviço está pronto")
                .contains("Acesse o portal: http://localhost:8080/api/publico/ordens-servico/" + ordemId + "/consulta")
                .contains("CPF e o Código de Acesso")
                .contains("Código de Acesso: ABC123");
    }

    @Test
    void naoExibeCorpoDaRequisicao() {
        when(ordemServicoJpaRepository.findById(ordemId)).thenReturn(Optional.of(ordemEntity()));

        new NotificadorPortalClienteAdapter(ordemServicoJpaRepository).aoGerarOrcamento(evento);

        assertThat(logs.list.getFirst().getFormattedMessage())
                .doesNotContain("POST")
                .doesNotContain("\"cpf\"");
    }

    @Test
    void ordemInexistenteNaoPropagaFalhaENaoNotifica() {
        when(ordemServicoJpaRepository.findById(ordemId)).thenReturn(Optional.empty());

        assertThatCode(() -> new NotificadorPortalClienteAdapter(ordemServicoJpaRepository).aoGerarOrcamento(evento))
                .doesNotThrowAnyException();

        assertThat(logs.list).hasSize(1);
        assertThat(logs.list.getFirst().getLevel()).isEqualTo(Level.ERROR);
        assertThat(logs.list.getFirst().getFormattedMessage()).doesNotContain("NOVO ORÇAMENTO GERADO");
    }
}
