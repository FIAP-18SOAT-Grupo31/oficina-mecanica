package br.com.fiap.oficina_mecanica.ordemservico.infrastructure.orcamento;

import br.com.fiap.oficina_mecanica.compartilhado.infrastructure.email.EnviadorEmail;
import br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.dto.ClienteDetalheOutput;
import br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.usecase.ConsultorClientes;
import br.com.fiap.oficina_mecanica.ordemservico.domain.evento.OrcamentoGerado;
import br.com.fiap.oficina_mecanica.ordemservico.infrastructure.ordemservico.OrdemServicoJpaRepository;
import br.com.fiap.oficina_mecanica.ordemservico.infrastructure.ordemservico.entity.OrdemServicoEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificadorEmailAdapterTest {

    private static final String URL_BASE = "http://oficina.test";

    @Mock
    private OrdemServicoJpaRepository ordemServicoJpaRepository;

    @Mock
    private ConsultorClientes consultorClientes;

    @Mock
    private EnviadorEmail enviadorEmail;

    private final UUID ordemId = UUID.randomUUID();
    private final UUID clienteId = UUID.randomUUID();
    private final UUID orcamentoId = UUID.randomUUID();

    private NotificadorEmailAdapter adapter() {
        return new NotificadorEmailAdapter(ordemServicoJpaRepository, consultorClientes, enviadorEmail, URL_BASE);
    }

    private OrdemServicoEntity ordem() {
        return new OrdemServicoEntity(ordemId, clienteId, UUID.randomUUID(), "Barulho na suspensão",
                br.com.fiap.oficina_mecanica.ordemservico.domain.ordemservico.StatusOrdemServico.AGUARDANDO_APROVACAO,
                LocalDateTime.now(), null, "ABC123");
    }

    @Test
    void enviaOPortalDoOrcamentoParaOEmailDoCliente() {
        when(ordemServicoJpaRepository.findById(ordemId)).thenReturn(Optional.of(ordem()));
        when(consultorClientes.detalharPorId(clienteId)).thenReturn(
                Optional.of(new ClienteDetalheOutput(clienteId, "Maria", "12345678901", "maria@exemplo.com", "11999999999")));

        adapter().aoGerarOrcamento(new OrcamentoGerado(orcamentoId, ordemId));

        ArgumentCaptor<String> assunto = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> corpo = ArgumentCaptor.forClass(String.class);
        verify(enviadorEmail).enviar(eq("maria@exemplo.com"), assunto.capture(), corpo.capture());
        assertThat(assunto.getValue()).isEqualTo("Novo orçamento gerado");
        assertThat(corpo.getValue())
                .contains("Acesse o portal: " + URL_BASE + "/api/publico/orcamentos/" + orcamentoId + "/consultar")
                .contains("Código de Acesso: ABC123");
    }

    @Test
    void clienteInexistenteNaoEnviaENaoPropagaErro() {
        when(ordemServicoJpaRepository.findById(ordemId)).thenReturn(Optional.of(ordem()));
        when(consultorClientes.detalharPorId(clienteId)).thenReturn(Optional.empty());

        assertThatCode(() -> adapter().aoGerarOrcamento(new OrcamentoGerado(orcamentoId, ordemId)))
                .doesNotThrowAnyException();

        verify(enviadorEmail, never()).enviar(any(), any(), any());
    }

    @Test
    void ordemInexistenteNaoPropagaErro() {
        when(ordemServicoJpaRepository.findById(ordemId)).thenReturn(Optional.empty());

        assertThatCode(() -> adapter().aoGerarOrcamento(new OrcamentoGerado(orcamentoId, ordemId)))
                .doesNotThrowAnyException();

        verify(enviadorEmail, never()).enviar(any(), any(), any());
    }
}
