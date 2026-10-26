package br.com.fiap.oficina_mecanica.ordemservico.infrastructure.orcamento;

import br.com.fiap.oficina_mecanica.compartilhado.domain.Horario;
import br.com.fiap.oficina_mecanica.ordemservico.domain.orcamento.Orcamento;
import br.com.fiap.oficina_mecanica.ordemservico.domain.ordemservico.StatusOrdemServico;
import br.com.fiap.oficina_mecanica.ordemservico.infrastructure.ordemservico.OrdemServicoJpaRepository;
import br.com.fiap.oficina_mecanica.ordemservico.infrastructure.ordemservico.entity.OrdemServicoEntity;
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

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificadorPortalClienteAdapterTest {

    @Mock
    private OrdemServicoJpaRepository ordemServicoJpaRepository;

    private final ListAppender<ILoggingEvent> logs = new ListAppender<>();
    private final Logger logger = (Logger) LoggerFactory.getLogger(NotificadorPortalClienteAdapter.class);

    private final UUID ordemId = UUID.randomUUID();
    private final Orcamento orcamento = new Orcamento(UUID.randomUUID(), ordemId, Horario.agora().plusDays(1));

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

        new NotificadorPortalClienteAdapter(ordemServicoJpaRepository).notificarOrcamentoGerado(orcamento);

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

        new NotificadorPortalClienteAdapter(ordemServicoJpaRepository).notificarOrcamentoGerado(orcamento);

        assertThat(logs.list.getFirst().getFormattedMessage())
                .doesNotContain("POST")
                .doesNotContain("\"cpf\"");
    }

    @Test
    void ordemInexistenteFalhaSemNotificar() {
        when(ordemServicoJpaRepository.findById(ordemId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> new NotificadorPortalClienteAdapter(ordemServicoJpaRepository)
                .notificarOrcamentoGerado(orcamento))
                .isInstanceOf(NoSuchElementException.class);
        assertThat(logs.list).isEmpty();
    }
}
