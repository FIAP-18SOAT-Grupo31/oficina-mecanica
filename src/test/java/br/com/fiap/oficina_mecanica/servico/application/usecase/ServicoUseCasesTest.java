package br.com.fiap.oficina_mecanica.servico.application.usecase;

import br.com.fiap.oficina_mecanica.compartilhado.application.dto.PaginaOutput;
import br.com.fiap.oficina_mecanica.compartilhado.domain.exception.RegraNegocioException;
import br.com.fiap.oficina_mecanica.servico.application.dto.AtualizarServicoInput;
import br.com.fiap.oficina_mecanica.servico.application.dto.CriarServicoInput;
import br.com.fiap.oficina_mecanica.servico.application.dto.ListarServicosInput;
import br.com.fiap.oficina_mecanica.servico.application.dto.ServicoOutput;
import br.com.fiap.oficina_mecanica.servico.domain.Servico;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ServicoUseCasesTest {

    private final ServicoRepositoryEmMemoria repository = new ServicoRepositoryEmMemoria();
    private final Set<UUID> servicosUsadosEmOrcamento = new HashSet<>();

    private final CriarServicoUseCase criar = new CriarServicoUseCase(repository);
    private final BuscarServicoUseCase buscar = new BuscarServicoUseCase(repository);
    private final ListarServicosUseCase listar = new ListarServicosUseCase(repository);
    private final AtualizarServicoUseCase atualizar = new AtualizarServicoUseCase(repository);
    private final ExcluirServicoUseCase excluir =
            new ExcluirServicoUseCase(repository, servicosUsadosEmOrcamento::contains);

    private Servico cadastrado(String nome) {
        return repository.salvar(new Servico(nome, null, new BigDecimal("100.00")));
    }

    // ---------------------------------------------------------------- criar

    @Test
    void criaServico() {
        ServicoOutput output = criar.execute(new CriarServicoInput(" Troca de óleo ", "Óleo e filtro",
                new BigDecimal("150.00")));

        assertThat(output.id()).isNotNull();
        assertThat(output.nome()).isEqualTo("Troca de óleo");
        assertThat(output.descricao()).isEqualTo("Óleo e filtro");
        assertThat(output.valorMaoDeObra()).isEqualByComparingTo("150.00");
        assertThat(repository.servicos).containsKey(output.id());
    }

    @Test
    void naoCriaServicoComNomeRepetidoIgnorandoMaiusculas() {
        cadastrado("Troca de óleo");

        assertThatThrownBy(() -> criar.execute(new CriarServicoInput("TROCA DE ÓLEO", null, BigDecimal.TEN)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Já existe um serviço cadastrado com este nome.");
        assertThat(repository.servicos).hasSize(1);
    }

    @Test
    void erroDeValidacaoDoDominioViraRegraDeNegocio() {
        assertThatThrownBy(() -> criar.execute(new CriarServicoInput("Alinhamento", null, BigDecimal.ZERO)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("O valor da mão de obra deve ser maior que zero");
    }

    // ---------------------------------------------------------------- buscar e listar

    @Test
    void buscaServicoPeloId() {
        Servico servico = cadastrado("Alinhamento");

        assertThat(buscar.execute(servico.getId()).nome()).isEqualTo("Alinhamento");
    }

    @Test
    void buscarServicoInexistenteLancaNaoEncontrado() {
        assertThatThrownBy(() -> buscar.execute(UUID.randomUUID()))
                .isInstanceOf(ServicoNaoEncontradoException.class)
                .hasMessage("Serviço não encontrado.");
    }

    @Test
    void listaServicosPaginadosEFiltradosPorNome() {
        cadastrado("Troca de óleo");
        cadastrado("Alinhamento");
        cadastrado("Balanceamento");

        PaginaOutput<ServicoOutput> todos = listar.execute(new ListarServicosInput("  ", 0, 2));
        PaginaOutput<ServicoOutput> filtrados = listar.execute(new ListarServicosInput(" amento ", 0, 10));

        assertThat(todos.conteudo()).extracting(ServicoOutput::nome).containsExactly("Alinhamento", "Balanceamento");
        assertThat(todos.totalElementos()).isEqualTo(3);
        assertThat(todos.totalPaginas()).isEqualTo(2);
        assertThat(filtrados.conteudo()).extracting(ServicoOutput::nome)
                .containsExactly("Alinhamento", "Balanceamento");
    }

    // ---------------------------------------------------------------- atualizar

    @Test
    void atualizaServico() {
        Servico servico = cadastrado("Alinhamento");

        ServicoOutput output = atualizar.execute(servico.getId(),
                new AtualizarServicoInput("Alinhamento 3D", "Com laudo", new BigDecimal("130.00")));

        assertThat(output.id()).isEqualTo(servico.getId());
        assertThat(output.nome()).isEqualTo("Alinhamento 3D");
        assertThat(output.descricao()).isEqualTo("Com laudo");
        assertThat(output.valorMaoDeObra()).isEqualByComparingTo("130.00");
    }

    @Test
    void atualizarMantendoOProprioNomeNaoConflita() {
        Servico servico = cadastrado("Alinhamento");

        ServicoOutput output = atualizar.execute(servico.getId(),
                new AtualizarServicoInput("ALINHAMENTO", null, new BigDecimal("90.00")));

        assertThat(output.nome()).isEqualTo("ALINHAMENTO");
    }

    @Test
    void naoAtualizaParaNomeDeOutroServico() {
        cadastrado("Troca de óleo");
        Servico servico = cadastrado("Alinhamento");

        assertThatThrownBy(() -> atualizar.execute(servico.getId(),
                new AtualizarServicoInput("troca de óleo", null, BigDecimal.TEN)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Já existe outro serviço cadastrado com este nome.");
    }

    @Test
    void atualizarServicoInexistenteLancaNaoEncontrado() {
        assertThatThrownBy(() -> atualizar.execute(UUID.randomUUID(),
                new AtualizarServicoInput("Alinhamento", null, BigDecimal.TEN)))
                .isInstanceOf(ServicoNaoEncontradoException.class);
    }

    @Test
    void atualizacaoInvalidaViraRegraDeNegocio() {
        Servico servico = cadastrado("Alinhamento");

        assertThatThrownBy(() -> atualizar.execute(servico.getId(),
                new AtualizarServicoInput("Alinhamento", null, new BigDecimal("-5"))))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("O valor da mão de obra deve ser maior que zero");
    }

    // ---------------------------------------------------------------- excluir

    @Test
    void excluiServicoNaoUsado() {
        Servico servico = cadastrado("Alinhamento");

        excluir.execute(servico.getId());

        assertThat(repository.servicos).isEmpty();
    }

    @Test
    void naoExcluiServicoUsadoEmOrcamento() {
        Servico servico = cadastrado("Alinhamento");
        servicosUsadosEmOrcamento.add(servico.getId());

        assertThatThrownBy(() -> excluir.execute(servico.getId()))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Não é possível excluir o serviço: ele já foi usado em orçamentos.");
        assertThat(repository.servicos).hasSize(1);
    }

    @Test
    void excluirServicoInexistenteLancaNaoEncontrado() {
        assertThatThrownBy(() -> excluir.execute(UUID.randomUUID()))
                .isInstanceOf(ServicoNaoEncontradoException.class);
    }
}
