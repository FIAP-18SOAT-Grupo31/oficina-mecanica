package br.com.fiap.oficina_mecanica.ordemservico.domain.ordemservico;

import br.com.fiap.oficina_mecanica.compartilhado.domain.Horario;
import br.com.fiap.oficina_mecanica.compartilhado.domain.exception.RegraNegocioException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.ArrayList;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

class OrdemServicoCicloDeVidaTest {

    private static final String RELATO = "Barulho na suspensão dianteira";

    private OrdemServico ordemComStatus(StatusOrdemServico status) {
        return OrdemServico.builder()
                .id(UUID.randomUUID()).clienteId(UUID.randomUUID()).veiculoId(UUID.randomUUID())
                .relatoProblema(RELATO).status(status).dataAbertura(Horario.agora())
                .orcamentos(new ArrayList<>())
                .build();
    }

    @ParameterizedTest
    @EnumSource(value = StatusOrdemServico.class, names = {"RECEBIDA", "EM_DIAGNOSTICO", "AGUARDANDO_APROVACAO"})
    void cancelaEnquantoNaoHouveAprovacao(StatusOrdemServico status) {
        OrdemServico ordem = ordemComStatus(status);

        ordem.cancelar();

        assertThat(ordem.getStatus()).isEqualTo(StatusOrdemServico.CANCELADA);
    }

    @ParameterizedTest
    @EnumSource(value = StatusOrdemServico.class, names = {"RECEBIDA", "EM_DIAGNOSTICO", "AGUARDANDO_APROVACAO"},
            mode = EnumSource.Mode.EXCLUDE)
    void naoCancelaDepoisDaAprovacaoOuQuandoJaEncerrada(StatusOrdemServico status) {
        OrdemServico ordem = ordemComStatus(status);

        assertThatThrownBy(ordem::cancelar)
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Uma OS com status " + status + " não pode ser cancelada.");
        assertThat(ordem.getStatus()).isEqualTo(status);
    }

    @Test
    void iniciaExecucaoDeOrdemAprovada() {
        OrdemServico ordem = ordemComStatus(StatusOrdemServico.APROVADA);

        ordem.iniciarExecucao();

        assertThat(ordem.getStatus()).isEqualTo(StatusOrdemServico.EM_EXECUCAO);
        assertThat(ordem.getDataConclusao()).isNull();
    }

    @ParameterizedTest
    @EnumSource(value = StatusOrdemServico.class, names = "APROVADA", mode = EnumSource.Mode.EXCLUDE)
    void naoIniciaExecucaoForaDoStatusAprovada(StatusOrdemServico status) {
        OrdemServico ordem = ordemComStatus(status);

        assertThatThrownBy(ordem::iniciarExecucao)
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("A execução só pode ser iniciada em uma OS aprovada.");
        assertThat(ordem.getStatus()).isEqualTo(status);
    }

    @Test
    void finalizaOrdemEmExecucaoRegistrandoADataDeConclusao() {
        OrdemServico ordem = ordemComStatus(StatusOrdemServico.EM_EXECUCAO);

        ordem.finalizar();

        assertThat(ordem.getStatus()).isEqualTo(StatusOrdemServico.FINALIZADA);
        assertThat(ordem.getDataConclusao()).isNotNull();
    }

    @ParameterizedTest
    @EnumSource(value = StatusOrdemServico.class, names = "EM_EXECUCAO", mode = EnumSource.Mode.EXCLUDE)
    void naoFinalizaForaDoStatusEmExecucao(StatusOrdemServico status) {
        OrdemServico ordem = ordemComStatus(status);

        assertThatThrownBy(ordem::finalizar)
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Apenas uma OS em execução pode ser finalizada.");
        assertThat(ordem.getStatus()).isEqualTo(status);
        assertThat(ordem.getDataConclusao()).isNull();
    }

    @Test
    void entregaOrdemFinalizadaMantendoADataDeConclusao() {
        OrdemServico ordem = ordemComStatus(StatusOrdemServico.EM_EXECUCAO);
        ordem.finalizar();
        var conclusao = ordem.getDataConclusao();

        ordem.entregar();

        assertThat(ordem.getStatus()).isEqualTo(StatusOrdemServico.ENTREGUE);
        assertThat(ordem.getDataConclusao()).isEqualTo(conclusao);
    }

    @ParameterizedTest
    @EnumSource(value = StatusOrdemServico.class, names = "FINALIZADA", mode = EnumSource.Mode.EXCLUDE)
    void naoEntregaForaDoStatusFinalizada(StatusOrdemServico status) {
        OrdemServico ordem = ordemComStatus(status);

        assertThatThrownBy(ordem::entregar)
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Apenas uma OS finalizada pode ser entregue.");
        assertThat(ordem.getStatus()).isEqualTo(status);
    }

    @ParameterizedTest
    @EnumSource(value = StatusOrdemServico.class, names = {"EM_EXECUCAO", "FINALIZADA", "ENTREGUE"})
    void ordemEmExecucaoOuPosteriorNaoPodeSerCancelada(StatusOrdemServico status) {
        assertThatThrownBy(ordemComStatus(status)::cancelar).isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void novaOrdemRegistraAberturaComoNovaTransicao() {
        OrdemServico ordem = new OrdemServico(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), RELATO);

        assertThat(ordem.getNovasTransicoes()).singleElement().satisfies(t -> {
            assertThat(t.statusAnterior()).isNull();
            assertThat(t.statusNovo()).isEqualTo(StatusOrdemServico.RECEBIDA);
            assertThat(t.dataTransicao()).isEqualTo(ordem.getDataAbertura());
        });
    }

    @Test
    void cadaTransicaoGeraUmRegistroEncadeado() {
        OrdemServico ordem = ordemComStatus(StatusOrdemServico.APROVADA);

        ordem.iniciarExecucao();
        ordem.finalizar();

        assertThat(ordem.getNovasTransicoes()).extracting(TransicaoStatus::statusAnterior, TransicaoStatus::statusNovo)
                .containsExactly(
                        tuple(StatusOrdemServico.APROVADA, StatusOrdemServico.EM_EXECUCAO),
                        tuple(StatusOrdemServico.EM_EXECUCAO, StatusOrdemServico.FINALIZADA));
        assertThat(ordem.getNovasTransicoes().get(1).dataTransicao()).isEqualTo(ordem.getDataConclusao());
    }

    @Test
    void transicaoInvalidaNaoRegistraTransicao() {
        OrdemServico ordem = ordemComStatus(StatusOrdemServico.RECEBIDA);

        assertThatThrownBy(ordem::finalizar).isInstanceOf(RegraNegocioException.class);

        assertThat(ordem.getNovasTransicoes()).isEmpty();
    }
}
