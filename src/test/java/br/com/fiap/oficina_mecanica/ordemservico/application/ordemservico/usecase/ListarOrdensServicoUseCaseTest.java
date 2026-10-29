package br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.usecase;

import br.com.fiap.oficina_mecanica.compartilhado.domain.Pagina;
import br.com.fiap.oficina_mecanica.compartilhado.domain.Paginacao;
import br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.dto.ClienteDetalheOutput;
import br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.dto.ClienteResumoOutput;
import br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.dto.OrdemServicoResumoOutput;
import br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.dto.VeiculoDetalheOutput;
import br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.dto.VeiculoResumoOutput;
import br.com.fiap.oficina_mecanica.ordemservico.domain.ordemservico.OrdemServico;
import br.com.fiap.oficina_mecanica.ordemservico.domain.ordemservico.OrdemServicoRepository;
import br.com.fiap.oficina_mecanica.ordemservico.domain.ordemservico.StatusOrdemServico;
import br.com.fiap.oficina_mecanica.ordemservico.domain.ordemservico.TempoExecucaoMedio;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ListarOrdensServicoUseCaseTest {

    private static final Paginacao PRIMEIRA_PAGINA = new Paginacao(0, 20);

    private static final class OrdemServicoRepositoryEmMemoria implements OrdemServicoRepository {

        private final List<OrdemServico> ordens = new ArrayList<>();

        @Override
        public OrdemServico salvar(OrdemServico ordemServico) {
            ordens.add(ordemServico);
            return ordemServico;
        }

        @Override
        public Optional<OrdemServico> buscarPorId(UUID id) {
            return ordens.stream().filter(o -> o.getId().equals(id)).findFirst();
        }

        @Override
        public Pagina<OrdemServico> listar(StatusOrdemServico status, Paginacao paginacao) {
            List<OrdemServico> filtradas = ordens.stream()
                    .filter(o -> status == null || o.getStatus() == status)
                    .sorted(Comparator.comparing(OrdemServico::getDataAbertura).reversed())
                    .toList();
            int inicio = Math.min(paginacao.pagina() * paginacao.tamanho(), filtradas.size());
            int fim = Math.min(inicio + paginacao.tamanho(), filtradas.size());
            return Pagina.de(filtradas.subList(inicio, fim), paginacao, filtradas.size());
        }

        @Override
        public boolean existePorVeiculoId(UUID veiculoId) {
            return ordens.stream().anyMatch(o -> o.getVeiculoId().equals(veiculoId));
        }

        @Override
        public TempoExecucaoMedio calcularTempoExecucaoMedio() {
            throw new UnsupportedOperationException();
        }
    }

    private static final class ConsultorClientesEmMemoria implements ConsultorClientes {

        private final Map<UUID, ClienteResumoOutput> clientes = new HashMap<>();
        private int chamadas;

        @Override
        public Map<UUID, ClienteResumoOutput> resumirPorIds(Collection<UUID> clienteIds) {
            chamadas++;
            Map<UUID, ClienteResumoOutput> encontrados = new HashMap<>(clientes);
            encontrados.keySet().retainAll(clienteIds);
            return encontrados;
        }

        @Override
        public Optional<ClienteDetalheOutput> detalharPorId(UUID clienteId) {
            return Optional.empty();
        }
    }

    private static final class ConsultorVeiculosEmMemoria implements ConsultorVeiculos {

        private final Map<UUID, VeiculoResumoOutput> veiculos = new HashMap<>();
        private int chamadas;

        @Override
        public Map<UUID, VeiculoResumoOutput> resumirPorIds(Collection<UUID> veiculoIds) {
            chamadas++;
            Map<UUID, VeiculoResumoOutput> encontrados = new HashMap<>(veiculos);
            encontrados.keySet().retainAll(veiculoIds);
            return encontrados;
        }

        @Override
        public Optional<VeiculoDetalheOutput> detalharPorId(UUID veiculoId) {
            return Optional.empty();
        }
    }

    private final OrdemServicoRepositoryEmMemoria repository = new OrdemServicoRepositoryEmMemoria();
    private final ConsultorClientesEmMemoria consultorClientes = new ConsultorClientesEmMemoria();
    private final ConsultorVeiculosEmMemoria consultorVeiculos = new ConsultorVeiculosEmMemoria();
    private final ListarOrdensServicoUseCase useCase =
            new ListarOrdensServicoUseCase(repository, consultorClientes, consultorVeiculos);

    private OrdemServico novaOrdem(String relato) {
        UUID clienteId = UUID.randomUUID();
        UUID veiculoId = UUID.randomUUID();
        consultorClientes.clientes.put(clienteId, new ClienteResumoOutput(clienteId, "Cliente " + relato));
        consultorVeiculos.veiculos.put(veiculoId, new VeiculoResumoOutput(veiculoId, "ABC1D23", "Fiat", "Argo"));
        return repository.salvar(new OrdemServico(UUID.randomUUID(), clienteId, veiculoId, relato));
    }

    @Test
    void semOrdensCadastradasDevolvePaginaVazia() {
        Pagina<OrdemServicoResumoOutput> pagina = useCase.execute(null, PRIMEIRA_PAGINA);

        assertThat(pagina.conteudo()).isEmpty();
        assertThat(pagina.totalElementos()).isZero();
        assertThat(pagina.totalPaginas()).isZero();
    }

    @Test
    void semFiltroDevolveTodasAsOrdens() {
        OrdemServico primeira = novaOrdem("Barulho na suspensão dianteira");
        OrdemServico segunda = novaOrdem("Luz do motor acesa no painel");
        segunda.iniciarDiagnostico();

        Pagina<OrdemServicoResumoOutput> pagina = useCase.execute(null, PRIMEIRA_PAGINA);

        assertThat(pagina.conteudo()).extracting(OrdemServicoResumoOutput::id)
                .containsExactlyInAnyOrder(primeira.getId(), segunda.getId());
        assertThat(pagina.totalElementos()).isEqualTo(2);
    }

    @Test
    void enriqueceComResumoDeClienteEVeiculo() {
        OrdemServico ordem = novaOrdem("Barulho na suspensão dianteira");

        OrdemServicoResumoOutput output = useCase.execute(null, PRIMEIRA_PAGINA).conteudo().getFirst();

        assertThat(output.id()).isEqualTo(ordem.getId());
        assertThat(output.status()).isEqualTo(StatusOrdemServico.RECEBIDA);
        assertThat(output.relatoProblema()).isEqualTo("Barulho na suspensão dianteira");
        assertThat(output.dataAbertura()).isEqualTo(ordem.getDataAbertura());
        assertThat(output.dataConclusao()).isNull();
        assertThat(output.cliente()).isEqualTo(consultorClientes.clientes.get(ordem.getClienteId()));
        assertThat(output.veiculo()).isEqualTo(consultorVeiculos.veiculos.get(ordem.getVeiculoId()));
    }

    @Test
    void clienteOuVeiculoNaoEncontradosFicamNulosSemQuebrarAListagem() {
        OrdemServico ordem = novaOrdem("Barulho na suspensão dianteira");
        consultorClientes.clientes.clear();
        consultorVeiculos.veiculos.clear();

        OrdemServicoResumoOutput output = useCase.execute(null, PRIMEIRA_PAGINA).conteudo().getFirst();

        assertThat(output.id()).isEqualTo(ordem.getId());
        assertThat(output.cliente()).isNull();
        assertThat(output.veiculo()).isNull();
    }

    @Test
    void buscaClientesEVeiculosEmLoteUmaVezPorListagem() {
        novaOrdem("Barulho na suspensão dianteira");
        novaOrdem("Luz do motor acesa no painel");
        novaOrdem("Freio fazendo ruído ao frear");

        useCase.execute(null, PRIMEIRA_PAGINA);

        assertThat(consultorClientes.chamadas).isEqualTo(1);
        assertThat(consultorVeiculos.chamadas).isEqualTo(1);
    }

    @Test
    void comFiltroDevolveApenasOrdensDoStatus() {
        OrdemServico recebida = novaOrdem("Barulho na suspensão dianteira");
        OrdemServico emDiagnostico = novaOrdem("Luz do motor acesa no painel");
        emDiagnostico.iniciarDiagnostico();

        assertThat(useCase.execute(StatusOrdemServico.RECEBIDA, PRIMEIRA_PAGINA).conteudo())
                .extracting(OrdemServicoResumoOutput::id).containsExactly(recebida.getId());
        assertThat(useCase.execute(StatusOrdemServico.EM_DIAGNOSTICO, PRIMEIRA_PAGINA).conteudo())
                .extracting(OrdemServicoResumoOutput::id).containsExactly(emDiagnostico.getId());
    }

    @Test
    void filtroSemOrdensNoStatusDevolvePaginaVazia() {
        novaOrdem("Barulho na suspensão dianteira");

        Pagina<OrdemServicoResumoOutput> pagina = useCase.execute(StatusOrdemServico.FINALIZADA, PRIMEIRA_PAGINA);

        assertThat(pagina.conteudo()).isEmpty();
        assertThat(pagina.totalElementos()).isZero();
    }

    @Test
    void paginaOsResultadosEInformaOTotal() {
        for (int i = 0; i < 5; i++) {
            novaOrdem("Problema numero " + i + " no veículo");
        }

        Pagina<OrdemServicoResumoOutput> primeira = useCase.execute(null, new Paginacao(0, 2));
        Pagina<OrdemServicoResumoOutput> ultima = useCase.execute(null, new Paginacao(2, 2));
        Pagina<OrdemServicoResumoOutput> alemDoFim = useCase.execute(null, new Paginacao(3, 2));

        assertThat(primeira.conteudo()).hasSize(2);
        assertThat(primeira.totalElementos()).isEqualTo(5);
        assertThat(primeira.totalPaginas()).isEqualTo(3);
        assertThat(ultima.conteudo()).hasSize(1);
        assertThat(alemDoFim.conteudo()).isEmpty();
        assertThat(alemDoFim.totalElementos()).isEqualTo(5);
    }
}
