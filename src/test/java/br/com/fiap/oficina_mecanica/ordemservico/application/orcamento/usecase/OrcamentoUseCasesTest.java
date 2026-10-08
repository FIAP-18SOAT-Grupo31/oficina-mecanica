package br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.usecase;

import br.com.fiap.oficina_mecanica.compartilhado.domain.Horario;
import br.com.fiap.oficina_mecanica.compartilhado.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.oficina_mecanica.compartilhado.domain.exception.RegraNegocioException;
import br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.dto.AprovacaoClienteInput;
import br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.dto.AprovacaoInput;
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

    private final VerificadorAcessoCliente verificadorAcesso = (osId, cpf, codigo) -> {
        if (!"12345678900".equals(cpf) || !"SENHA123".equals(codigo)) {
            throw new RegraNegocioException("Credenciais inválidas");
        }
    };

    private final GerarOrcamentoUseCase gerarOrcamento = new GerarOrcamentoUseCase(repository, notificados::add);
    private final AprovarOrcamentoUseCase aprovarOrcamento = new AprovarOrcamentoUseCase(repository);
    private final RejeitarOrcamentoUseCase rejeitarOrcamento = new RejeitarOrcamentoUseCase(repository);
    private final BuscarOrcamentoUseCase buscarOrcamento = new BuscarOrcamentoUseCase(repository);
    private final AprovarOrcamentoClienteUseCase aprovarOrcamentoCliente = new AprovarOrcamentoClienteUseCase(repository, verificadorAcesso);
    private final RejeitarOrcamentoClienteUseCase rejeitarOrcamentoCliente = new RejeitarOrcamentoClienteUseCase(repository, verificadorAcesso);
    private final BuscarOrcamentoClienteUseCase buscarOrcamentoCliente = new BuscarOrcamentoClienteUseCase(repository, verificadorAcesso);

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
    void aprovaOrcamentoExistenteTotalmente() {
        OrcamentoOutput gerado = gerarOrcamento.execute(entrada(List.of(), List.of()));

        OrcamentoOutput aprovado = aprovarOrcamento.execute(gerado.id(), null);

        assertThat(aprovado.status()).isEqualTo(StatusOrcamento.APROVADO);
        assertThat(repository.buscarPorId(gerado.id()).orElseThrow().getStatus()).isEqualTo(StatusOrcamento.APROVADO);
    }

    @Test
    void aprovaOrcamentoParcialmenteRecalculandoTotal() {
        UUID servicoId = UUID.randomUUID();
        UUID pecaAprovadaId = UUID.randomUUID();
        UUID pecaRejeitadaId = UUID.randomUUID();

        GerarOrcamentoInput input = entrada(
                List.of(new ItemServicoInput(servicoId, "Servico 1", new BigDecimal("100.00"))),
                List.of(
                        new ItemPecaInput(pecaAprovadaId, "Peca 1", 2, new BigDecimal("50.00")),
                        new ItemPecaInput(pecaRejeitadaId, "Peca 2", 1, new BigDecimal("30.00"))
                )
        );

        OrcamentoOutput gerado = gerarOrcamento.execute(input);

        AprovacaoInput aprovacaoParcial = new AprovacaoInput(List.of(servicoId), List.of(pecaAprovadaId));
        OrcamentoOutput aprovado = aprovarOrcamento.execute(gerado.id(), aprovacaoParcial);

        assertThat(aprovado.status()).isEqualTo(StatusOrcamento.APROVADO_PARCIALMENTE);

        assertThat(aprovado.valorTotal()).isEqualByComparingTo("200.00");
    }

    @Test
    void naoAprovaOrcamentoInexistente() {
        UUID inexistente = UUID.randomUUID();
        assertThatThrownBy(() -> aprovarOrcamento.execute(inexistente, null))
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

    @Test
    void buscaOrcamentoPeloClienteComCredenciaisValidas() {
        OrcamentoOutput gerado = gerarOrcamento.execute(entrada(List.of(), List.of()));
        OrcamentoOutput encontrado = buscarOrcamentoCliente.execute(gerado.id(), "12345678900", "SENHA123");
        assertThat(encontrado.id()).isEqualTo(gerado.id());
    }

    @Test
    void naoBuscaOrcamentoPeloClienteComCredenciaisInvalidas() {
        OrcamentoOutput gerado = gerarOrcamento.execute(entrada(List.of(), List.of()));
        assertThatThrownBy(() -> buscarOrcamentoCliente.execute(gerado.id(), "000", "ERRADO"))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("Credenciais inválidas");
    }

    @Test
    void aprovaOrcamentoPeloClienteComCredenciaisValidas() {
        OrcamentoOutput gerado = gerarOrcamento.execute(entrada(List.of(), List.of()));

        AprovacaoClienteInput input = new AprovacaoClienteInput("12345678900", "SENHA123", null, null);
        OrcamentoOutput aprovado = aprovarOrcamentoCliente.execute(gerado.id(), input);

        assertThat(aprovado.status()).isEqualTo(StatusOrcamento.APROVADO);
    }

    @Test
    void naoAprovaOrcamentoPeloClienteComCredenciaisInvalidas() {
        OrcamentoOutput gerado = gerarOrcamento.execute(entrada(List.of(), List.of()));
        AprovacaoClienteInput input = new AprovacaoClienteInput("000", "ERRADO", null, null);

        assertThatThrownBy(() -> aprovarOrcamentoCliente.execute(gerado.id(), input))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("Credenciais inválidas");
    }

    @Test
    void rejeitaOrcamentoPeloClienteComCredenciaisValidas() {
        OrcamentoOutput gerado = gerarOrcamento.execute(entrada(List.of(), List.of()));
        OrcamentoOutput rejeitado = rejeitarOrcamentoCliente.execute(gerado.id(), "12345678900", "SENHA123");
        assertThat(rejeitado.status()).isEqualTo(StatusOrcamento.REJEITADO);
    }
}