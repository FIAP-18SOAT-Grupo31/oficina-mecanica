package br.com.fiap.oficina_mecanica.ordemservico.domain.orcamento;

import br.com.fiap.oficina_mecanica.compartilhado.domain.Horario;
import br.com.fiap.oficina_mecanica.compartilhado.domain.exception.RegraNegocioException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrcamentoTest {

    private Orcamento novoOrcamento(LocalDateTime validade) {
        return new Orcamento(UUID.randomUUID(), UUID.randomUUID(), validade);
    }

    private ItemServico servico(String valor) {
        return new ItemServico(UUID.randomUUID(), "Servico teste", new BigDecimal(valor));
    }

    private ItemPeca peca(int qtd, String valorUnitario) {
        return new ItemPeca(UUID.randomUUID(), "Peca teste", qtd, new BigDecimal(valorUnitario));
    }

    @Test
    void nascePendenteSemItensEComValorZero() {
        Orcamento orcamento = novoOrcamento(Horario.agora().plusDays(1));

        assertThat(orcamento.getStatus()).isEqualTo(StatusOrcamento.PENDENTE);
        assertThat(orcamento.getServicos()).isEmpty();
        assertThat(orcamento.getPecas()).isEmpty();
        assertThat(orcamento.calcularValorTotal()).isEqualByComparingTo("0");
    }

    @Test
    void naoPodeNascerComIdNulo() {
        assertThatThrownBy(() -> new Orcamento(null, UUID.randomUUID(), Horario.agora()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void somaOValorTotalDosServicosEDasPecas() {
        Orcamento orcamento = novoOrcamento(Horario.agora().plusDays(1));
        orcamento.adicionarServico(servico("100.50"));
        orcamento.adicionarPeca(peca(2, "30.00"));

        BigDecimal total = orcamento.calcularValorTotal();

        assertThat(total).isEqualByComparingTo("160.50");
    }

    @Test
    void orcamentoPendentePodeSerAprovadoTotalmente() {
        Orcamento orcamento = novoOrcamento(Horario.agora().plusDays(1));

        orcamento.aprovar(null, null);

        assertThat(orcamento.getStatus()).isEqualTo(StatusOrcamento.APROVADO);
    }

    @Test
    void orcamentoAprovadoNaoPodeSerAprovadoNovamente() {
        Orcamento orcamento = novoOrcamento(Horario.agora().plusDays(1));
        orcamento.aprovar(null, null);

        assertThatThrownBy(() -> orcamento.aprovar(null, null))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("Apenas orçamentos pendentes");
    }

    @Test
    void orcamentoPendentePodeSerRejeitado() {
        Orcamento orcamento = novoOrcamento(Horario.agora().plusDays(1));

        orcamento.rejeitar();

        assertThat(orcamento.getStatus()).isEqualTo(StatusOrcamento.REJEITADO);
    }

    @Test
    void orcamentoVencidoNaoPodeSerAprovado() {
        Orcamento orcamento = novoOrcamento(Horario.agora().minusDays(1));

        assertThatThrownBy(() -> orcamento.aprovar(null, null))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("expirado");

        assertThat(orcamento.getStatus()).isEqualTo(StatusOrcamento.EXPIRADO);
    }

    @Test
    void naoPodeAdicionarItensSeOrcamentoNaoEstiverPendente() {
        Orcamento aprovado = novoOrcamento(Horario.agora().plusDays(1));
        aprovado.aprovar(null, null);

        Orcamento rejeitado = novoOrcamento(Horario.agora().plusDays(1));
        rejeitado.rejeitar();

        assertThatThrownBy(() -> aprovado.adicionarServico(servico("10")))
                .isInstanceOf(RegraNegocioException.class);

        assertThatThrownBy(() -> rejeitado.adicionarPeca(peca(1, "10")))
                .isInstanceOf(RegraNegocioException.class);
    }
}