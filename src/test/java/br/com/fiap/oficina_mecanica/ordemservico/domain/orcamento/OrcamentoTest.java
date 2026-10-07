package br.com.fiap.oficina_mecanica.ordemservico.domain.orcamento;

import br.com.fiap.oficina_mecanica.compartilhado.domain.exception.RegraNegocioException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrcamentoTest {

    private final UUID ordemServicoId = UUID.randomUUID();

    private Orcamento orcamentoValido() {
        return new Orcamento(UUID.randomUUID(), ordemServicoId, LocalDateTime.now().plusDays(7));
    }

    @Test
    void novoOrcamentoComecaPendenteESemItens() {
        Orcamento orcamento = orcamentoValido();

        assertThat(orcamento.getId()).isNotNull();
        assertThat(orcamento.getOrdemServicoId()).isEqualTo(ordemServicoId);
        assertThat(orcamento.getStatus()).isEqualTo(StatusOrcamento.PENDENTE);
        assertThat(orcamento.getDataCriacao()).isNotNull();
        assertThat(orcamento.getServicos()).isEmpty();
        assertThat(orcamento.getPecas()).isEmpty();
        assertThat(orcamento.calcularValorTotal()).isEqualByComparingTo("0");
    }

    @Test
    void valorTotalSomaMaoDeObraEPecas() {
        Orcamento orcamento = orcamentoValido();
        orcamento.adicionarServico(new ItemServico(UUID.randomUUID(), "Troca de óleo", new BigDecimal("100.00")));
        orcamento.adicionarServico(new ItemServico(UUID.randomUUID(), "Alinhamento", new BigDecimal("80.00")));
        orcamento.adicionarPeca(new ItemPeca(UUID.randomUUID(), "Óleo 5W30", 4, new BigDecimal("40.00")));

        assertThat(orcamento.calcularValorTotal()).isEqualByComparingTo("340.00");
    }

    @Test
    void itensDoOrcamentoNaoPodemSerAlteradosPorFora() {
        Orcamento orcamento = orcamentoValido();
        List<ItemServico> servicos = orcamento.getServicos();
        ItemServico item = new ItemServico(UUID.randomUUID(), "Freio", BigDecimal.TEN);

        assertThatThrownBy(() -> servicos.add(item)).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void aprovaOrcamentoPendenteDentroDaValidade() {
        Orcamento orcamento = orcamentoValido();

        orcamento.aprovar();

        assertThat(orcamento.getStatus()).isEqualTo(StatusOrcamento.APROVADO);
    }

    @Test
    void orcamentoVencidoExpiraAoTentarAprovar() {
        Orcamento orcamento = new Orcamento(UUID.randomUUID(), ordemServicoId, LocalDateTime.now().minusMinutes(1));

        assertThatThrownBy(orcamento::aprovar)
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("expirado");
        assertThat(orcamento.getStatus()).isEqualTo(StatusOrcamento.EXPIRADO);
    }

    @Test
    void rejeitaOrcamentoPendente() {
        Orcamento orcamento = orcamentoValido();

        orcamento.rejeitar();

        assertThat(orcamento.getStatus()).isEqualTo(StatusOrcamento.REJEITADO);
    }

    @Test
    void naoAprovaNemRejeitaOrcamentoJaDecidido() {
        Orcamento aprovado = orcamentoValido();
        aprovado.aprovar();
        Orcamento rejeitado = orcamentoValido();
        rejeitado.rejeitar();

        assertThatThrownBy(aprovado::aprovar).isInstanceOf(RegraNegocioException.class);
        assertThatThrownBy(aprovado::rejeitar).isInstanceOf(RegraNegocioException.class);
        assertThatThrownBy(rejeitado::aprovar).isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void naoAceitaNovosItensDepoisDeDecidido() {
        Orcamento orcamento = orcamentoValido();
        orcamento.aprovar();
        ItemServico servico = new ItemServico(UUID.randomUUID(), "Revisão", BigDecimal.ONE);
        ItemPeca peca = new ItemPeca(UUID.randomUUID(), "Filtro", 1, BigDecimal.ONE);

        assertThatThrownBy(() -> orcamento.adicionarServico(servico)).isInstanceOf(RegraNegocioException.class);
        assertThatThrownBy(() -> orcamento.adicionarPeca(peca)).isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void reconstroiOrcamentoPersistidoComOsMesmosDados() {
        UUID id = UUID.randomUUID();
        LocalDateTime criacao = LocalDateTime.now().minusDays(1);
        LocalDateTime validade = LocalDateTime.now().plusDays(6);
        List<ItemServico> servicos = List.of(new ItemServico(UUID.randomUUID(), "Revisão", new BigDecimal("150.00")));
        List<ItemPeca> pecas = List.of(new ItemPeca(UUID.randomUUID(), "Filtro", 2, new BigDecimal("25.00")));

        Orcamento orcamento = new Orcamento(id, ordemServicoId, criacao, validade, StatusOrcamento.PENDENTE, servicos, pecas);

        assertThat(orcamento.getId()).isEqualTo(id);
        assertThat(orcamento.getDataCriacao()).isEqualTo(criacao);
        assertThat(orcamento.getDataValidade()).isEqualTo(validade);
        assertThat(orcamento.calcularValorTotal()).isEqualByComparingTo("200.00");
    }

    @Test
    void itensCalculamOProprioTotal() {
        assertThat(new ItemPeca(UUID.randomUUID(), "Vela", 4, new BigDecimal("12.50")).calcularTotal())
                .isEqualByComparingTo("50.00");
        assertThat(new ItemServico(UUID.randomUUID(), "Mão de obra", new BigDecimal("90.00")).calcularTotal())
                .isEqualByComparingTo("90.00");
    }
}
