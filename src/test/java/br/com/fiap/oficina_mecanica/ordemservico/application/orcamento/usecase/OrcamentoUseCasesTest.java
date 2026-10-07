package br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.usecase;

import br.com.fiap.oficina_mecanica.compartilhado.domain.Horario;
import br.com.fiap.oficina_mecanica.compartilhado.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.dto.GerarOrcamentoInput;
import br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.dto.ItemPecaInput;
import br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.dto.ItemServicoInput;
import br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.dto.OrcamentoOutput;
import br.com.fiap.oficina_mecanica.ordemservico.domain.orcamento.Orcamento;
import br.com.fiap.oficina_mecanica.ordemservico.domain.orcamento.StatusOrcamento;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrcamentoUseCasesTest {

    private final OrcamentoRepositoryEmMemoria repository = new OrcamentoRepositoryEmMemoria();
    private final List<Orcamento> notificados = new ArrayList<>();
    private final GerarOrcamentoUseCase gerarOrcamento = new GerarOrcamentoUseCase(repository, notificados::add);
    private final AprovarOrcamentoUseCase aprovarOrcamento = new AprovarOrcamentoUseCase(repository);
    private final RejeitarOrcamentoUseCase rejeitarOrcamento = new RejeitarOrcamentoUseCase(repository);
    private final BuscarOrcamentoUseCase buscarOrcamento = new BuscarOrcamentoUseCase(repository);

    private GerarOrcamentoInput entrada(List<ItemServicoInput> servicos, List<ItemPecaInput> pecas) {
        return new GerarOrcamentoInput(UUID.randomUUID(), Horario.agora().plusDays(7), servicos, pecas);
    }

    @Test
    void geraOrcamentoComServicosEPecasCalculandoOTotal() {
        GerarOrcamentoInput input = entrada(
                List.of(new ItemServicoInput(UUID.randomUUID(), "Troca de óleo", new BigDecimal("100.00"))),
                List.of(new ItemPecaInput(UUID.randomUUID(), "Óleo 5W30", 4, new BigDecimal("40.00"))));

        OrcamentoOutput output = gerarOrcamento.execute(input);

        assertThat(output.id()).isNotNull();
        assertThat(output.ordemServicoId()).isEqualTo(input.ordemServicoId());
        assertThat(output.valorTotal()).isEqualByComparingTo("260.00");
        assertThat(output.status()).isEqualTo(StatusOrcamento.PENDENTE);
        assertThat(repository.buscarPorId(output.id())).isPresent();
    }

    @Test
    void notificaOClienteAoGerarOrcamento() {
        OrcamentoOutput output = gerarOrcamento.execute(entrada(List.of(), List.of()));

        assertThat(notificados).extracting(Orcamento::getId).containsExactly(output.id());
    }

    @Test
    void geraOrcamentoSemItensQuandoAsListasNaoSaoInformadas() {
        OrcamentoOutput output = gerarOrcamento.execute(entrada(null, null));

        assertThat(output.valorTotal()).isEqualByComparingTo("0");
    }

    @Test
    void aprovaOrcamentoExistente() {
        OrcamentoOutput gerado = gerarOrcamento.execute(entrada(List.of(), List.of()));

        OrcamentoOutput aprovado = aprovarOrcamento.execute(gerado.id());

        assertThat(aprovado.status()).isEqualTo(StatusOrcamento.APROVADO);
        assertThat(repository.buscarPorId(gerado.id()).orElseThrow().getStatus()).isEqualTo(StatusOrcamento.APROVADO);
    }

    @Test
    void naoAprovaOrcamentoInexistente() {
        UUID inexistente = UUID.randomUUID();

        assertThatThrownBy(() -> aprovarOrcamento.execute(inexistente))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("não encontrado");
    }

    @Test
    void rejeitaOrcamentoExistente() {
        OrcamentoOutput gerado = gerarOrcamento.execute(entrada(List.of(), List.of()));

        OrcamentoOutput rejeitado = rejeitarOrcamento.execute(gerado.id());

        assertThat(rejeitado.status()).isEqualTo(StatusOrcamento.REJEITADO);
    }

    @Test
    void naoRejeitaOrcamentoInexistente() {
        UUID inexistente = UUID.randomUUID();

        assertThatThrownBy(() -> rejeitarOrcamento.execute(inexistente))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("não encontrado");
    }

    @Test
    void buscaOrcamentoPeloId() {
        OrcamentoOutput gerado = gerarOrcamento.execute(entrada(List.of(), List.of()));

        OrcamentoOutput encontrado = buscarOrcamento.execute(gerado.id());

        assertThat(encontrado.id()).isEqualTo(gerado.id());
        assertThat(encontrado.status()).isEqualTo(StatusOrcamento.PENDENTE);
    }

    @Test
    void buscaDeOrcamentoInexistenteFalha() {
        UUID inexistente = UUID.randomUUID();

        assertThatThrownBy(() -> buscarOrcamento.execute(inexistente))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("não encontrado");
    }
}
