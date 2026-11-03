package br.com.fiap.oficina_mecanica.cliente.infrastructure;

import br.com.fiap.oficina_mecanica.cliente.domain.Cliente;
import br.com.fiap.oficina_mecanica.cliente.domain.ClienteRepository;
import br.com.fiap.oficina_mecanica.cliente.domain.Endereco;
import br.com.fiap.oficina_mecanica.cliente.domain.Estado;
import br.com.fiap.oficina_mecanica.compartilhado.domain.Pagina;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@TesteIntegracaoCliente
class ClienteRepositoryAdapterIntegrationTest {

    private static final String CPF_ANA_SOUZA = "529.982.247-25";
    private static final String CPF_BRUNO = "000.000.037-00";
    private static final String CPF_ANA_PAULA = "000.000.074-47";
    private static final String CPF_DESCONTO = "000.000.370-01";
    private static final String CPF_CAMINHO = "000.000.999-70";

    @Autowired
    private ClienteRepository repository;

    @Autowired
    private EntityManager entityManager;

    private static Endereco endereco(String complemento) {
        return new Endereco("Rua das Flores", "123", complemento, "Centro", "São Paulo", Estado.SP, "01001-000");
    }

    private static Cliente cliente(String nome, String documento) {
        return new Cliente(documento, nome, "(11) 98765-4321", "cliente@email.com", endereco(null));
    }

    private void gravarNoBanco() {
        entityManager.flush();
        entityManager.clear();
    }

    private void cadastrarClientesParaListagem() {
        repository.salvar(cliente("Ana Souza", CPF_ANA_SOUZA));
        repository.salvar(cliente("Bruno Lima", CPF_BRUNO));
        repository.salvar(cliente("Ana Paula", CPF_ANA_PAULA));
        repository.salvar(cliente("Desconto 100%", CPF_DESCONTO));
        repository.salvar(cliente("Caminho\\Teste", CPF_CAMINHO));
        gravarNoBanco();
    }

    private static List<String> nomes(Pagina<Cliente> pagina) {
        return pagina.conteudo().stream().map(Cliente::getNome).toList();
    }

    @Test
    void salvaEBuscaOClienteComTodosOsDadosIncluindoComplemento() {
        Cliente cliente = new Cliente(CPF_ANA_SOUZA, "Ana Souza", "(11) 98765-4321", "ana@email.com",
                endereco("Apto 12"));

        Cliente salvo = repository.salvar(cliente);
        gravarNoBanco();
        Cliente encontrado = repository.buscarPorId(cliente.getId()).orElseThrow();

        assertThat(salvo.getId()).isEqualTo(cliente.getId());
        assertThat(encontrado.getId()).isEqualTo(cliente.getId());
        assertThat(encontrado.getNome()).isEqualTo("Ana Souza");
        assertThat(encontrado.getDocumento()).isEqualTo("52998224725");
        assertThat(encontrado.getTelefone()).isEqualTo("11987654321");
        assertThat(encontrado.getEmail()).isEqualTo("ana@email.com");
        assertThat(encontrado.getEndereco().getRua()).isEqualTo("Rua Das Flores");
        assertThat(encontrado.getEndereco().getNumero()).isEqualTo("123");
        assertThat(encontrado.getEndereco().getComplemento()).isEqualTo("Apto 12");
        assertThat(encontrado.getEndereco().getBairro()).isEqualTo("Centro");
        assertThat(encontrado.getEndereco().getCidade()).isEqualTo("São Paulo");
        assertThat(encontrado.getEndereco().getEstado()).isEqualTo(Estado.SP);
        assertThat(encontrado.getEndereco().getCep()).isEqualTo("01001000");
    }

    @Test
    void salvaClienteSemComplemento() {
        Cliente cliente = cliente("Ana Souza", CPF_ANA_SOUZA);

        repository.salvar(cliente);
        gravarNoBanco();

        assertThat(repository.buscarPorId(cliente.getId()).orElseThrow().getEndereco().getComplemento()).isNull();
    }

    @Test
    void salvarClienteExistenteAtualizaOsDados() {
        Cliente cliente = repository.salvar(cliente("Ana Souza", CPF_ANA_SOUZA));
        gravarNoBanco();

        cliente.atualizar(CPF_BRUNO, "Bruno Lima", "(21) 3456-7890", "bruno@email.com", endereco("Fundos"));
        repository.salvar(cliente);
        gravarNoBanco();

        Cliente atualizado = repository.buscarPorId(cliente.getId()).orElseThrow();
        assertThat(atualizado.getNome()).isEqualTo("Bruno Lima");
        assertThat(atualizado.getDocumento()).isEqualTo("00000003700");
        assertThat(atualizado.getTelefone()).isEqualTo("2134567890");
        assertThat(atualizado.getEndereco().getComplemento()).isEqualTo("Fundos");
    }

    @Test
    void buscarPorIdDeClienteInexistenteRetornaVazio() {
        assertThat(repository.buscarPorId(UUID.randomUUID())).isEmpty();
    }

    @Test
    void verificaExistenciaPorId() {
        Cliente cliente = repository.salvar(cliente("Ana Souza", CPF_ANA_SOUZA));
        gravarNoBanco();

        assertThat(repository.existePorId(cliente.getId())).isTrue();
        assertThat(repository.existePorId(UUID.randomUUID())).isFalse();
    }

    @Test
    void verificaExistenciaPorDocumento() {
        repository.salvar(cliente("Ana Souza", CPF_ANA_SOUZA));
        gravarNoBanco();

        assertThat(repository.existePorDocumento("52998224725")).isTrue();
        assertThat(repository.existePorDocumento("00000003700")).isFalse();
    }

    @Test
    void verificaExistenciaDeOutroClienteComOMesmoDocumento() {
        Cliente cliente = repository.salvar(cliente("Ana Souza", CPF_ANA_SOUZA));
        gravarNoBanco();

        assertThat(repository.existeOutroComDocumento("52998224725", cliente.getId())).isFalse();
        assertThat(repository.existeOutroComDocumento("52998224725", UUID.randomUUID())).isTrue();
        assertThat(repository.existeOutroComDocumento("00000003700", UUID.randomUUID())).isFalse();
    }

    @Test
    void buscaODocumentoPorId() {
        Cliente cliente = repository.salvar(cliente("Ana Souza", CPF_ANA_SOUZA));
        gravarNoBanco();

        assertThat(repository.buscarDocumentoPorId(cliente.getId())).contains("52998224725");
        assertThat(repository.buscarDocumentoPorId(UUID.randomUUID())).isEmpty();
    }

    @Test
    void excluiOClientePorId() {
        Cliente cliente = repository.salvar(cliente("Ana Souza", CPF_ANA_SOUZA));
        gravarNoBanco();

        repository.excluirPorId(cliente.getId());
        gravarNoBanco();

        assertThat(repository.existePorId(cliente.getId())).isFalse();
        assertThat(repository.buscarPorId(cliente.getId())).isEmpty();
    }

    @Test
    void listaSemFiltrosOrdenadoPorNome() {
        cadastrarClientesParaListagem();

        Pagina<Cliente> pagina = repository.listar(null, null, 0, 10);

        assertThat(nomes(pagina))
                .containsExactly("Ana Paula", "Ana Souza", "Bruno Lima", "Caminho\\Teste", "Desconto 100%");
        assertThat(pagina.pagina()).isZero();
        assertThat(pagina.tamanho()).isEqualTo(10);
        assertThat(pagina.totalElementos()).isEqualTo(5);
        assertThat(pagina.totalPaginas()).isEqualTo(1);
    }

    @Test
    void paginaOsResultados() {
        cadastrarClientesParaListagem();

        Pagina<Cliente> primeira = repository.listar(null, null, 0, 2);
        Pagina<Cliente> ultima = repository.listar(null, null, 2, 2);
        Pagina<Cliente> alemDoFim = repository.listar(null, null, 5, 2);

        assertThat(nomes(primeira)).containsExactly("Ana Paula", "Ana Souza");
        assertThat(primeira.totalElementos()).isEqualTo(5);
        assertThat(primeira.totalPaginas()).isEqualTo(3);
        assertThat(nomes(ultima)).containsExactly("Desconto 100%");
        assertThat(alemDoFim.conteudo()).isEmpty();
        assertThat(alemDoFim.totalElementos()).isEqualTo(5);
    }

    @Test
    void filtraPorNomeSemDiferenciarMaiusculasEPorTrechoDoNome() {
        cadastrarClientesParaListagem();

        Pagina<Cliente> pagina = repository.listar("ANA", null, 0, 10);
        Pagina<Cliente> trecho = repository.listar("rUn", null, 0, 10);

        assertThat(nomes(pagina)).containsExactly("Ana Paula", "Ana Souza");
        assertThat(pagina.totalElementos()).isEqualTo(2);
        assertThat(nomes(trecho)).containsExactly("Bruno Lima");
    }

    @Test
    void filtraPorDocumentoExato() {
        cadastrarClientesParaListagem();

        assertThat(nomes(repository.listar(null, "00000003700", 0, 10))).containsExactly("Bruno Lima");
        assertThat(repository.listar(null, "0000000370", 0, 10).conteudo()).isEmpty();
    }

    @Test
    void combinaOsFiltrosDeNomeEDocumento() {
        cadastrarClientesParaListagem();

        assertThat(nomes(repository.listar("lima", "00000003700", 0, 10))).containsExactly("Bruno Lima");
        assertThat(repository.listar("ana", "00000003700", 0, 10).conteudo()).isEmpty();
    }

    @Test
    void tratamOsCuringasDoLikeComoTextoLiteral() {
        cadastrarClientesParaListagem();

        assertThat(nomes(repository.listar("%", null, 0, 10))).containsExactly("Desconto 100%");
        assertThat(repository.listar("_", null, 0, 10).conteudo()).isEmpty();
        assertThat(nomes(repository.listar("\\", null, 0, 10))).containsExactly("Caminho\\Teste");
    }
}
