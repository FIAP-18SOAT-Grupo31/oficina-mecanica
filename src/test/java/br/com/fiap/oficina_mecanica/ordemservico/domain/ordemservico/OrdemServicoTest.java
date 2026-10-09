package br.com.fiap.oficina_mecanica.ordemservico.domain.ordemservico;

import br.com.fiap.oficina_mecanica.compartilhado.domain.Horario;
import br.com.fiap.oficina_mecanica.compartilhado.domain.exception.RegraNegocioException;
import br.com.fiap.oficina_mecanica.ordemservico.domain.orcamento.Orcamento;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrdemServicoTest {

    private static final String RELATO = "Barulho na suspensão dianteira";

    private OrdemServico novaOrdem() {
        return new OrdemServico(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), RELATO);
    }

    private Orcamento novoOrcamento(OrdemServico ordemServico) {
        return new Orcamento(UUID.randomUUID(), ordemServico.getId(), Horario.agora().plusDays(7));
    }

    @Test
    void novaOrdemComecaRecebidaComDataDeAberturaESemOrcamentos() {
        OrdemServico ordemServico = novaOrdem();

        assertThat(ordemServico.getStatus()).isEqualTo(StatusOrdemServico.RECEBIDA);
        assertThat(ordemServico.getDataAbertura()).isNotNull();
        assertThat(ordemServico.getDataConclusao()).isNull();
        assertThat(ordemServico.getRelatoProblema()).isEqualTo(RELATO);
        assertThat(ordemServico.getOrcamentos()).isEmpty();
    }

    @Test
    void naoAceitaIdNulo() {
        UUID clienteId = UUID.randomUUID();
        UUID veiculoId = UUID.randomUUID();

        assertThatThrownBy(() -> new OrdemServico(null, clienteId, veiculoId, RELATO))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void naoAceitaRelatoVazio() {
        UUID id = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        UUID veiculoId = UUID.randomUUID();

        assertThatThrownBy(() -> new OrdemServico(id, clienteId, veiculoId, " "))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("O relato do problema não pode ser nulo ou vazio.");
    }

    @Test
    void naoAceitaRelatoForaDoTamanho() {
        UUID id = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        UUID veiculoId = UUID.randomUUID();
        String relatoLongo = "a".repeat(501);

        assertThatThrownBy(() -> new OrdemServico(id, clienteId, veiculoId, "curto"))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("O relato do problema deve ter entre 10 e 500 caracteres.");
        assertThatThrownBy(() -> new OrdemServico(id, clienteId, veiculoId, relatoLongo))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void anexarOrcamentoDepoisDoDiagnosticoAguardaAprovacao() {
        OrdemServico ordemServico = novaOrdem();
        ordemServico.iniciarDiagnostico();
        Orcamento orcamento = novoOrcamento(ordemServico);

        ordemServico.anexarOrcamento(orcamento);

        assertThat(ordemServico.getStatus()).isEqualTo(StatusOrdemServico.AGUARDANDO_APROVACAO);
        assertThat(ordemServico.getOrcamentos()).containsExactly(orcamento);
    }

    @Test
    void diagnosticoSoComecaEmOrdemRecebida() {
        OrdemServico ordemServico = novaOrdem();
        ordemServico.iniciarDiagnostico();

        assertThatThrownBy(ordemServico::iniciarDiagnostico).isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void orcamentoSoEhAnexadoDuranteODiagnostico() {
        OrdemServico ordemServico = novaOrdem();
        Orcamento orcamento = novoOrcamento(ordemServico);

        assertThatThrownBy(() -> ordemServico.anexarOrcamento(orcamento))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void aprovarOuRecusarExigeOrdemAguardandoAprovacao() {
        OrdemServico ordemServico = novaOrdem();

        assertThatThrownBy(ordemServico::aprovarOrcamento).isInstanceOf(RegraNegocioException.class);
        assertThatThrownBy(ordemServico::recusarOrcamento).isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void aprovaOrcamentoQuandoAguardandoAprovacao() {
        OrdemServico ordemServico = novaOrdem();
        ordemServico.iniciarDiagnostico();
        ordemServico.anexarOrcamento(novoOrcamento(ordemServico));

        ordemServico.aprovarOrcamento();

        assertThat(ordemServico.getStatus()).isEqualTo(StatusOrdemServico.APROVADA);
    }

    @Test
    void recusaOrcamentoQuandoAguardandoAprovacao() {
        OrdemServico ordemServico = novaOrdem();
        ordemServico.iniciarDiagnostico();
        ordemServico.anexarOrcamento(novoOrcamento(ordemServico));

        ordemServico.recusarOrcamento();

        assertThat(ordemServico.getStatus()).isEqualTo(StatusOrdemServico.RECUSADA);
    }

    @Test
    void nasceComCodigoDeAcessoDeSeisCaracteres() {
        OrdemServico os = new OrdemServico(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "Problema no motor");

        assertThat(os.getCodigoAcesso()).isNotNull();
        assertThat(os.getCodigoAcesso().length()).isEqualTo(6);
    }
}
