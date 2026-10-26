package br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.usecase;

import br.com.fiap.oficina_mecanica.compartilhado.domain.Horario;
import br.com.fiap.oficina_mecanica.compartilhado.domain.Pagina;
import br.com.fiap.oficina_mecanica.compartilhado.domain.Paginacao;
import br.com.fiap.oficina_mecanica.compartilhado.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.oficina_mecanica.compartilhado.domain.exception.RegraNegocioException;
import br.com.fiap.oficina_mecanica.estoque.domain.ProdutoEstoque;
import br.com.fiap.oficina_mecanica.estoque.domain.ProdutoEstoqueRepository;
import br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.dto.AprovacaoClienteInput;
import br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.dto.AprovacaoInput;
import br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.dto.GerarOrcamentoInput;
import br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.dto.ItemPecaInput;
import br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.dto.ItemServicoInput;
import br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.dto.OrcamentoOutput;
import br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.usecase.NotificadorOrdemServico;
import br.com.fiap.oficina_mecanica.ordemservico.domain.orcamento.ItemPeca;
import br.com.fiap.oficina_mecanica.ordemservico.domain.orcamento.Orcamento;
import br.com.fiap.oficina_mecanica.ordemservico.domain.orcamento.StatusOrcamento;
import br.com.fiap.oficina_mecanica.ordemservico.domain.ordemservico.OrdemServico;
import br.com.fiap.oficina_mecanica.ordemservico.domain.ordemservico.OrdemServicoRepository;
import br.com.fiap.oficina_mecanica.ordemservico.domain.ordemservico.StatusOrdemServico;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrcamentoUseCasesTest {

    private final OrcamentoRepositoryEmMemoria repository = new OrcamentoRepositoryEmMemoria();
    private final OrdemServicoRepositoryEmMemoria osRepository = new OrdemServicoRepositoryEmMemoria();
    private final ProdutoEstoqueRepositoryEmMemoria estoqueRepository = new ProdutoEstoqueRepositoryEmMemoria();
    private final List<Orcamento> notificados = new ArrayList<>();
    private final List<String> avisosOrdem = new ArrayList<>();

    private final NotificadorOrdemServico notificadorOrdemServico = new NotificadorOrdemServico() {
        @Override
        public void notificarOrdemAprovada(OrdemServico ordemServico, boolean aprovacaoParcial) {
            avisosOrdem.add(aprovacaoParcial ? "APROVADA_PARCIALMENTE" : "APROVADA");
        }

        @Override
        public void notificarOrdemRecusada(OrdemServico ordemServico) {
            avisosOrdem.add("RECUSADA");
        }

        @Override
        public void notificarOrdemCancelada(OrdemServico ordemServico) {
            avisosOrdem.add("CANCELADA");
        }

        @Override
        public void notificarOrdemFinalizada(OrdemServico ordemServico) {
            avisosOrdem.add("FINALIZADA");
        }
    };

    private final VerificadorAcessoCliente verificadorAcesso = (osId, cpf, codigo) -> {
        if (!"12345678900".equals(cpf) || !"SENHA123".equals(codigo)) {
            throw new RegraNegocioException("Credenciais inválidas");
        }
    };

    private GerarOrcamentoUseCase gerarOrcamento;
    private AprovarOrcamentoUseCase aprovarOrcamento;
    private RejeitarOrcamentoUseCase rejeitarOrcamento;
    private BuscarOrcamentoUseCase buscarOrcamento;
    private AprovarOrcamentoClienteUseCase aprovarOrcamentoCliente;
    private RejeitarOrcamentoClienteUseCase rejeitarOrcamentoCliente;
    private BuscarOrcamentoClienteUseCase buscarOrcamentoCliente;

    private UUID ordemServicoIdFixa;
    private UUID pecaComEstoqueId;
    private UUID pecaSemEstoqueId;

    @BeforeEach
    void setup() {
        gerarOrcamento = new GerarOrcamentoUseCase(repository, List.of(notificados::add), osRepository, estoqueRepository);
        aprovarOrcamento = new AprovarOrcamentoUseCase(repository, osRepository, estoqueRepository, notificadorOrdemServico);
        rejeitarOrcamento = new RejeitarOrcamentoUseCase(repository, osRepository, estoqueRepository, notificadorOrdemServico);
        buscarOrcamento = new BuscarOrcamentoUseCase(repository);
        aprovarOrcamentoCliente = new AprovarOrcamentoClienteUseCase(repository, verificadorAcesso, aprovarOrcamento);
        rejeitarOrcamentoCliente = new RejeitarOrcamentoClienteUseCase(repository, verificadorAcesso, rejeitarOrcamento);
        buscarOrcamentoCliente = new BuscarOrcamentoClienteUseCase(repository, verificadorAcesso);

        ordemServicoIdFixa = UUID.randomUUID();
        OrdemServico os = new OrdemServico(ordemServicoIdFixa, UUID.randomUUID(), UUID.randomUUID(), "Relato valido longo suficiente");
        osRepository.salvar(os);

        pecaComEstoqueId = UUID.randomUUID();
        ProdutoEstoque pecaComEstoque = new ProdutoEstoque(pecaComEstoqueId, "Filtro de Óleo", 10, new BigDecimal("40.00"));
        estoqueRepository.salvar(pecaComEstoque);

        pecaSemEstoqueId = UUID.randomUUID();
        ProdutoEstoque pecaSemEstoque = new ProdutoEstoque(pecaSemEstoqueId, "Correia Dentada", 0, new BigDecimal("120.00"));
        estoqueRepository.salvar(pecaSemEstoque);
    }

    private GerarOrcamentoInput entrada(List<ItemServicoInput> servicos, List<ItemPecaInput> pecas) {
        return new GerarOrcamentoInput(ordemServicoIdFixa, Horario.agora().plusDays(7), servicos, pecas);
    }

    @Test
    void geraOrcamentoComServicosEPecasCalculandoOTotalEReservandoEstoque() {
        GerarOrcamentoInput input = entrada(
                List.of(new ItemServicoInput(UUID.randomUUID(), "Troca de óleo", new BigDecimal("100.00"))),
                List.of(new ItemPecaInput(pecaComEstoqueId, "Filtro de Óleo", 4, new BigDecimal("40.00"))));

        OrcamentoOutput output = gerarOrcamento.execute(input);

        assertThat(output.id()).isNotNull();
        assertThat(output.valorTotal()).isEqualByComparingTo("260.00");
        assertThat(output.status()).isEqualTo(StatusOrcamento.PENDENTE);
        assertThat(output.servicos()).hasSize(1);
        assertThat(output.pecas()).hasSize(1);

        ProdutoEstoque estoque = estoqueRepository.buscarPorId(pecaComEstoqueId).orElseThrow();
        assertThat(estoque.getQuantidadeDisponivel()).isEqualTo(6);
        assertThat(estoque.getQuantidadeReservada()).isEqualTo(4);
    }

    @Test
    void falhaAoGerarOrcamentoSeEstoqueInexistente() {
        GerarOrcamentoInput input = entrada(
                List.of(),
                List.of(new ItemPecaInput(pecaSemEstoqueId, "Correia Dentada", 1, new BigDecimal("120.00"))));

        assertThatThrownBy(() -> gerarOrcamento.execute(input))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("Estoque insuficiente");
    }

    @Test
    void falhaAoGerarOrcamentoComDataValidadePassada() {
        GerarOrcamentoInput inputInvalido = new GerarOrcamentoInput(
                ordemServicoIdFixa,
                Horario.agora().minusDays(1),
                List.of(),
                List.of()
        );

        assertThatThrownBy(() -> gerarOrcamento.execute(inputInvalido))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("validade");
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
    void aprovaOrcamentoExistenteTotalmenteEAvançaOSDaBaixaNoEstoque() {
        OrcamentoOutput gerado = gerarOrcamento.execute(entrada(
                List.of(),
                List.of(new ItemPecaInput(pecaComEstoqueId, "Filtro de Óleo", 2, new BigDecimal("40.00")))
        ));

        OrcamentoOutput aprovado = aprovarOrcamento.execute(gerado.id(), null);

        assertThat(aprovado.status()).isEqualTo(StatusOrcamento.APROVADO);
        assertThat(osRepository.buscarPorId(ordemServicoIdFixa).orElseThrow().getStatus()).isEqualTo(StatusOrdemServico.APROVADA);
        assertThat(avisosOrdem).containsExactly("APROVADA");

        ProdutoEstoque estoque = estoqueRepository.buscarPorId(pecaComEstoqueId).orElseThrow();
        assertThat(estoque.getQuantidadeDisponivel()).isEqualTo(8);
        assertThat(estoque.getQuantidadeReservada()).isEqualTo(0);
    }

    @Test
    void aprovaOrcamentoParcialmenteRecalculandoTotal() {
        UUID servicoId = UUID.randomUUID();

        GerarOrcamentoInput input = entrada(
                List.of(new ItemServicoInput(servicoId, "Servico 1", new BigDecimal("100.00"))),
                List.of(new ItemPecaInput(pecaComEstoqueId, "Peca 1", 2, new BigDecimal("40.00")))
        );

        OrcamentoOutput gerado = gerarOrcamento.execute(input);

        AprovacaoInput aprovacaoParcial = new AprovacaoInput(List.of(servicoId), List.of());
        OrcamentoOutput aprovado = aprovarOrcamento.execute(gerado.id(), aprovacaoParcial);

        assertThat(aprovado.status()).isEqualTo(StatusOrcamento.APROVADO_PARCIALMENTE);
        assertThat(aprovado.valorTotal()).isEqualByComparingTo("100.00");
        assertThat(avisosOrdem).containsExactly("APROVADA_PARCIALMENTE");

        ProdutoEstoque estoque = estoqueRepository.buscarPorId(pecaComEstoqueId).orElseThrow();
        assertThat(estoque.getQuantidadeDisponivel()).isEqualTo(10);
        assertThat(estoque.getQuantidadeReservada()).isEqualTo(0);
    }

    @Test
    void aprovarComTodosItensRecusadosRejeitaOrcamentoERecusaOSDevolvendoEstoque() {
        UUID servicoId = UUID.randomUUID();

        GerarOrcamentoInput input = entrada(
                List.of(new ItemServicoInput(servicoId, "Servico 1", new BigDecimal("100.00"))),
                List.of(new ItemPecaInput(pecaComEstoqueId, "Peca 1", 2, new BigDecimal("40.00")))
        );

        OrcamentoOutput gerado = gerarOrcamento.execute(input);

        AprovacaoInput recusaTudo = new AprovacaoInput(List.of(), List.of());
        OrcamentoOutput resultado = aprovarOrcamento.execute(gerado.id(), recusaTudo);

        assertThat(resultado.status()).isEqualTo(StatusOrcamento.REJEITADO);
        assertThat(osRepository.buscarPorId(ordemServicoIdFixa).orElseThrow().getStatus()).isEqualTo(StatusOrdemServico.RECUSADA);
        assertThat(avisosOrdem).containsExactly("RECUSADA");

        ProdutoEstoque estoque = estoqueRepository.buscarPorId(pecaComEstoqueId).orElseThrow();
        assertThat(estoque.getQuantidadeDisponivel()).isEqualTo(10);
        assertThat(estoque.getQuantidadeReservada()).isEqualTo(0);
    }

    @Test
    void aprovarOrcamentoExpiradoPersisteStatusExpiradoELiberaEstoque() {
        ProdutoEstoque produto = estoqueRepository.buscarPorId(pecaComEstoqueId).orElseThrow();
        produto.reservar(3);
        estoqueRepository.salvar(produto);

        OrdemServico os = osRepository.buscarPorId(ordemServicoIdFixa).orElseThrow();
        os.iniciarDiagnostico();

        UUID orcamentoExpiradoId = UUID.randomUUID();
        Orcamento orcamentoExpirado = new Orcamento(
                orcamentoExpiradoId,
                ordemServicoIdFixa,
                Horario.agora().minusDays(10),
                Horario.agora().minusDays(3),
                StatusOrcamento.PENDENTE,
                List.of(),
                List.of(new ItemPeca(pecaComEstoqueId, "Filtro de Óleo", 3, new BigDecimal("40.00")))
        );
        os.anexarOrcamento(orcamentoExpirado);
        osRepository.salvar(os);
        repository.salvar(orcamentoExpirado);

        assertThatThrownBy(() -> aprovarOrcamento.execute(orcamentoExpiradoId, null))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("expirado");

        assertThat(repository.buscarPorId(orcamentoExpiradoId).orElseThrow().getStatus())
                .isEqualTo(StatusOrcamento.EXPIRADO);
        assertThat(osRepository.buscarPorId(ordemServicoIdFixa).orElseThrow().getStatus())
                .isEqualTo(StatusOrdemServico.RECUSADA);
        assertThat(avisosOrdem).containsExactly("RECUSADA");

        ProdutoEstoque estoqueAtualizado = estoqueRepository.buscarPorId(pecaComEstoqueId).orElseThrow();
        assertThat(estoqueAtualizado.getQuantidadeDisponivel()).isEqualTo(10);
        assertThat(estoqueAtualizado.getQuantidadeReservada()).isEqualTo(0);
    }

    @Test
    void naoAprovaOrcamentoInexistente() {
        UUID inexistente = UUID.randomUUID();
        assertThatThrownBy(() -> aprovarOrcamento.execute(inexistente, null))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("não encontrado");
        assertThat(avisosOrdem).isEmpty();
    }

    @Test
    void rejeitarOrcamentoAvisaQueAOrdemFoiRecusada() {
        OrcamentoOutput gerado = gerarOrcamento.execute(entrada(List.of(), List.of()));

        rejeitarOrcamento.execute(gerado.id());

        assertThat(avisosOrdem).containsExactly("RECUSADA");
    }

    @Test
    void gerarOrcamentoNaoAvisaSobreAOrdem() {
        gerarOrcamento.execute(entrada(List.of(), List.of()));

        assertThat(avisosOrdem).isEmpty();
    }

    @Test
    void aprovarDeNovoUmOrcamentoJaAprovadoFalhaSemNovoAviso() {
        OrcamentoOutput gerado = gerarOrcamento.execute(entrada(List.of(), List.of()));
        aprovarOrcamento.execute(gerado.id(), null);

        assertThatThrownBy(() -> aprovarOrcamento.execute(gerado.id(), null))
                .isInstanceOf(RegraNegocioException.class);

        assertThat(avisosOrdem).containsExactly("APROVADA");
    }

    @Test
    void rejeitarOrcamentoInexistenteNaoAvisa() {
        UUID inexistente = UUID.randomUUID();

        assertThatThrownBy(() -> rejeitarOrcamento.execute(inexistente))
                .isInstanceOf(RecursoNaoEncontradoException.class);

        assertThat(avisosOrdem).isEmpty();
    }

    @Test
    void rejeitaOrcamentoExistenteEDevolveEstoque() {
        OrcamentoOutput gerado = gerarOrcamento.execute(entrada(
                List.of(),
                List.of(new ItemPecaInput(pecaComEstoqueId, "Filtro de Óleo", 3, new BigDecimal("40.00")))
        ));

        OrcamentoOutput rejeitado = rejeitarOrcamento.execute(gerado.id());

        assertThat(rejeitado.status()).isEqualTo(StatusOrcamento.REJEITADO);
        assertThat(osRepository.buscarPorId(ordemServicoIdFixa).orElseThrow().getStatus()).isEqualTo(StatusOrdemServico.RECUSADA);

        ProdutoEstoque estoque = estoqueRepository.buscarPorId(pecaComEstoqueId).orElseThrow();
        assertThat(estoque.getQuantidadeDisponivel()).isEqualTo(10);
        assertThat(estoque.getQuantidadeReservada()).isEqualTo(0);
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
        assertThat(osRepository.buscarPorId(ordemServicoIdFixa).orElseThrow().getStatus()).isEqualTo(StatusOrdemServico.APROVADA);
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
        assertThat(osRepository.buscarPorId(ordemServicoIdFixa).orElseThrow().getStatus()).isEqualTo(StatusOrdemServico.RECUSADA);
    }

    private static class OrdemServicoRepositoryEmMemoria implements OrdemServicoRepository {
        private final Map<UUID, OrdemServico> ordens = new HashMap<>();

        @Override
        public OrdemServico salvar(OrdemServico ordemServico) {
            ordens.put(ordemServico.getId(), ordemServico);
            return ordemServico;
        }

        @Override
        public Optional<OrdemServico> buscarPorId(UUID id) {
            return Optional.ofNullable(ordens.get(id));
        }

        @Override
        public Pagina<OrdemServico> listar(StatusOrdemServico status, Paginacao paginacao) {
            throw new UnsupportedOperationException();
        }
    }

    private static class ProdutoEstoqueRepositoryEmMemoria implements ProdutoEstoqueRepository {
        private final Map<UUID, ProdutoEstoque> estoque = new HashMap<>();

        @Override
        public ProdutoEstoque salvar(ProdutoEstoque produto) {
            estoque.put(produto.getId(), produto);
            return produto;
        }

        @Override
        public Optional<ProdutoEstoque> buscarPorId(UUID id) {
            return Optional.ofNullable(estoque.get(id));
        }
    }
}