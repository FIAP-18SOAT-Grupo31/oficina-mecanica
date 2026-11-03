package br.com.fiap.oficina_mecanica.veiculo.infrastructure;

import br.com.fiap.oficina_mecanica.cliente.domain.Cliente;
import br.com.fiap.oficina_mecanica.cliente.domain.ClienteRepository;
import br.com.fiap.oficina_mecanica.cliente.domain.Endereco;
import br.com.fiap.oficina_mecanica.cliente.domain.Estado;
import br.com.fiap.oficina_mecanica.cliente.infrastructure.TesteIntegracaoCliente;
import br.com.fiap.oficina_mecanica.compartilhado.domain.Pagina;
import br.com.fiap.oficina_mecanica.veiculo.domain.Veiculo;
import br.com.fiap.oficina_mecanica.veiculo.domain.VeiculoRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@TesteIntegracaoCliente
class VeiculoRepositoryAdapterIntegrationTest {

    @Autowired
    private VeiculoRepository repository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private EntityManager entityManager;

    private UUID clienteA;
    private UUID clienteB;

    private UUID cadastrarCliente(String documento) {
        Cliente cliente = new Cliente(documento, "Cliente " + documento, "(11) 98765-4321", "cliente@email.com",
                new Endereco("Rua das Flores", "123", null, "Centro", "São Paulo", Estado.SP, "01001-000"));
        return clienteRepository.salvar(cliente).getId();
    }

    private void gravarNoBanco() {
        entityManager.flush();
        entityManager.clear();
    }

    private static List<String> placas(Pagina<Veiculo> pagina) {
        return pagina.conteudo().stream().map(Veiculo::getPlaca).toList();
    }

    @BeforeEach
    void cadastrarClientes() {
        clienteA = cadastrarCliente("529.982.247-25");
        clienteB = cadastrarCliente("000.000.037-00");
        gravarNoBanco();
    }

    private void cadastrarVeiculosParaListagem() {
        repository.salvar(new Veiculo(2022, "ABC1D23", "Argo", "Fiat", clienteA));
        repository.salvar(new Veiculo(2018, "ABC1234", "Civic", "Honda", clienteA));
        repository.salvar(new Veiculo(2020, "XYZ9K88", "Civic 100%", "Honda", clienteB));
        repository.salvar(new Veiculo(2015, "QWE1R23", "Gol", "VW", clienteB));
        gravarNoBanco();
    }

    @Test
    void salvaEBuscaOVeiculoComTodosOsDados() {
        Veiculo veiculo = new Veiculo(2022, "abc-1d23", "Argo", "Fiat", clienteA);

        Veiculo salvo = repository.salvar(veiculo);
        gravarNoBanco();
        Veiculo encontrado = repository.buscarPorId(veiculo.getId()).orElseThrow();

        assertThat(salvo.getId()).isEqualTo(veiculo.getId());
        assertThat(encontrado.getId()).isEqualTo(veiculo.getId());
        assertThat(encontrado.getClienteId()).isEqualTo(clienteA);
        assertThat(encontrado.getPlaca()).isEqualTo("ABC1D23");
        assertThat(encontrado.getMarca()).isEqualTo("Fiat");
        assertThat(encontrado.getModelo()).isEqualTo("Argo");
        assertThat(encontrado.getAnoFabricacao()).isEqualTo(2022);
    }

    @Test
    void salvarVeiculoExistenteAtualizaOsDados() {
        Veiculo veiculo = repository.salvar(new Veiculo(2022, "ABC1D23", "Argo", "Fiat", clienteA));
        gravarNoBanco();

        veiculo.atualizar(2020, "XYZ9K88", "Civic", "Honda", clienteB);
        repository.salvar(veiculo);
        gravarNoBanco();

        Veiculo atualizado = repository.buscarPorId(veiculo.getId()).orElseThrow();
        assertThat(atualizado.getPlaca()).isEqualTo("XYZ9K88");
        assertThat(atualizado.getClienteId()).isEqualTo(clienteB);
        assertThat(atualizado.getAnoFabricacao()).isEqualTo(2020);
    }

    @Test
    void buscarPorIdDeVeiculoInexistenteRetornaVazio() {
        assertThat(repository.buscarPorId(UUID.randomUUID())).isEmpty();
    }

    @Test
    void verificaExistenciaPorIdEPorCliente() {
        Veiculo veiculo = repository.salvar(new Veiculo(2022, "ABC1D23", "Argo", "Fiat", clienteA));
        gravarNoBanco();

        assertThat(repository.existePorId(veiculo.getId())).isTrue();
        assertThat(repository.existePorId(UUID.randomUUID())).isFalse();
        assertThat(repository.existePorClienteId(clienteA)).isTrue();
        assertThat(repository.existePorClienteId(clienteB)).isFalse();
    }

    @Test
    void verificaExistenciaPorPlacaEDeOutroVeiculoComAMesmaPlaca() {
        Veiculo veiculo = repository.salvar(new Veiculo(2022, "ABC1D23", "Argo", "Fiat", clienteA));
        gravarNoBanco();

        assertThat(repository.existePorPlaca("ABC1D23")).isTrue();
        assertThat(repository.existePorPlaca("XYZ9K88")).isFalse();
        assertThat(repository.existeOutroComPlaca("ABC1D23", veiculo.getId())).isFalse();
        assertThat(repository.existeOutroComPlaca("ABC1D23", UUID.randomUUID())).isTrue();
        assertThat(repository.existeOutroComPlaca("XYZ9K88", UUID.randomUUID())).isFalse();
    }

    @Test
    void excluiOVeiculoPorId() {
        Veiculo veiculo = repository.salvar(new Veiculo(2022, "ABC1D23", "Argo", "Fiat", clienteA));
        gravarNoBanco();

        repository.excluirPorId(veiculo.getId());
        gravarNoBanco();

        assertThat(repository.existePorId(veiculo.getId())).isFalse();
    }

    @Test
    void listaSemFiltrosOrdenadoPorPlaca() {
        cadastrarVeiculosParaListagem();

        Pagina<Veiculo> pagina = repository.listar(null, null, null, 0, 10);

        assertThat(placas(pagina)).containsExactly("ABC1234", "ABC1D23", "QWE1R23", "XYZ9K88");
        assertThat(pagina.totalElementos()).isEqualTo(4);
        assertThat(pagina.totalPaginas()).isEqualTo(1);
    }

    @Test
    void paginaOsResultados() {
        cadastrarVeiculosParaListagem();

        Pagina<Veiculo> primeira = repository.listar(null, null, null, 0, 3);
        Pagina<Veiculo> segunda = repository.listar(null, null, null, 1, 3);
        Pagina<Veiculo> alemDoFim = repository.listar(null, null, null, 5, 3);

        assertThat(placas(primeira)).containsExactly("ABC1234", "ABC1D23", "QWE1R23");
        assertThat(primeira.totalPaginas()).isEqualTo(2);
        assertThat(placas(segunda)).containsExactly("XYZ9K88");
        assertThat(alemDoFim.conteudo()).isEmpty();
        assertThat(alemDoFim.totalElementos()).isEqualTo(4);
    }

    @Test
    void filtraPorClienteEPlacaExata() {
        cadastrarVeiculosParaListagem();

        assertThat(placas(repository.listar(clienteA, null, null, 0, 10))).containsExactly("ABC1234", "ABC1D23");
        assertThat(placas(repository.listar(null, "QWE1R23", null, 0, 10))).containsExactly("QWE1R23");
        assertThat(repository.listar(null, "QWE1R2", null, 0, 10).conteudo()).isEmpty();
        assertThat(repository.listar(clienteA, "QWE1R23", null, 0, 10).conteudo()).isEmpty();
    }

    @Test
    void filtraPorModeloSemDiferenciarMaiusculasETratandoCuringasComoTexto() {
        cadastrarVeiculosParaListagem();

        assertThat(placas(repository.listar(null, null, "CIVIC", 0, 10))).containsExactly("ABC1234", "XYZ9K88");
        assertThat(placas(repository.listar(null, null, "%", 0, 10))).containsExactly("XYZ9K88");
        assertThat(repository.listar(null, null, "_", 0, 10).conteudo()).isEmpty();
        assertThat(placas(repository.listar(clienteB, "XYZ9K88", "civic", 0, 10))).containsExactly("XYZ9K88");
    }
}
