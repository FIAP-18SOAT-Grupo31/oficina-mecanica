package br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.usecase;

import br.com.fiap.oficina_mecanica.compartilhado.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.oficina_mecanica.compartilhado.domain.exception.RegraNegocioException;
import br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.dto.CriarOrdemServicoInput;
import br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.dto.CriarOrdemServicoOutput;
import br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.usecase.VerificadorVeiculo.VeiculoCadastrado;
import br.com.fiap.oficina_mecanica.ordemservico.domain.ordemservico.OrdemServico;
import br.com.fiap.oficina_mecanica.ordemservico.domain.ordemservico.OrdemServicoRepository;
import br.com.fiap.oficina_mecanica.ordemservico.domain.ordemservico.StatusOrdemServico;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CriarOrdemServicoUseCaseTest {

    private static final class OrdemServicoRepositoryEmMemoria implements OrdemServicoRepository {

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
        public boolean existePorVeiculoId(UUID veiculoId) {
            return ordens.values().stream().anyMatch(ordem -> ordem.getVeiculoId().equals(veiculoId));
        }
    }

    private static final class CadastroEmMemoria implements VerificadorCliente, VerificadorVeiculo {

        private final Map<UUID, String> documentos = new HashMap<>();
        private final Map<UUID, VeiculoCadastrado> veiculos = new HashMap<>();
        private final Map<String, UUID> placas = new HashMap<>();

        UUID cliente(String documento) {
            UUID id = UUID.randomUUID();
            documentos.put(id, documento);
            return id;
        }

        UUID veiculo(UUID clienteId, String placa) {
            UUID id = UUID.randomUUID();
            veiculos.put(id, new VeiculoCadastrado(id, clienteId));
            placas.put(placa, id);
            return id;
        }

        @Override
        public boolean existe(UUID clienteId) {
            return documentos.containsKey(clienteId);
        }

        @Override
        public Optional<UUID> buscarIdPorDocumento(String documento) {
            return documentos.entrySet().stream()
                    .filter(entrada -> entrada.getValue().equals(documento))
                    .map(Map.Entry::getKey)
                    .findFirst();
        }

        @Override
        public Optional<VeiculoCadastrado> buscarPorId(UUID veiculoId) {
            return Optional.ofNullable(veiculos.get(veiculoId));
        }

        @Override
        public Optional<VeiculoCadastrado> buscarPorPlaca(String placa) {
            return Optional.ofNullable(placas.get(placa)).map(veiculos::get);
        }
    }

    private final OrdemServicoRepositoryEmMemoria repository = new OrdemServicoRepositoryEmMemoria();
    private final CadastroEmMemoria cadastro = new CadastroEmMemoria();
    private final CriarOrdemServicoUseCase useCase = new CriarOrdemServicoUseCase(cadastro, cadastro, repository);
    private final UUID clienteId = cadastro.cliente("52998224725");
    private final UUID veiculoId = cadastro.veiculo(clienteId, "ABC1D23");

    private static final String RELATO = "Barulho na suspensão dianteira";

    @Test
    void criaOrdemRecebidaParaClienteEVeiculoExistentes() {
        CriarOrdemServicoOutput output = useCase.execute(new CriarOrdemServicoInput(clienteId, veiculoId, RELATO));

        OrdemServico salva = repository.buscarPorId(output.ordemServicoId()).orElseThrow();
        assertThat(salva.getClienteId()).isEqualTo(clienteId);
        assertThat(salva.getVeiculoId()).isEqualTo(veiculoId);
        assertThat(salva.getStatus()).isEqualTo(StatusOrdemServico.RECEBIDA);
    }

    @Test
    void identificaClientePeloDocumentoEVeiculoPelaPlacaIgnorandoPontuacao() {
        CriarOrdemServicoOutput output = useCase.execute(
                new CriarOrdemServicoInput(null, "529.982.247-25", null, "abc-1d23", RELATO));

        OrdemServico salva = repository.buscarPorId(output.ordemServicoId()).orElseThrow();
        assertThat(salva.getClienteId()).isEqualTo(clienteId);
        assertThat(salva.getVeiculoId()).isEqualTo(veiculoId);
    }

    @Test
    void clienteInexistenteViraRecursoNaoEncontrado() {
        assertThatThrownBy(() -> useCase.execute(new CriarOrdemServicoInput(UUID.randomUUID(), veiculoId, RELATO)))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Cliente não encontrado.");
        assertThat(repository.ordens).isEmpty();
    }

    @Test
    void documentoSemClienteViraRecursoNaoEncontrado() {
        assertThatThrownBy(() -> useCase.execute(
                new CriarOrdemServicoInput(null, "111.444.777-35", veiculoId, null, RELATO)))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Cliente não encontrado para o CPF/CNPJ informado.");
        assertThat(repository.ordens).isEmpty();
    }

    @Test
    void veiculoInexistenteViraRecursoNaoEncontrado() {
        assertThatThrownBy(() -> useCase.execute(new CriarOrdemServicoInput(clienteId, UUID.randomUUID(), RELATO)))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Veículo não encontrado.");
        assertThat(repository.ordens).isEmpty();
    }

    @Test
    void placaSemVeiculoViraRecursoNaoEncontrado() {
        assertThatThrownBy(() -> useCase.execute(new CriarOrdemServicoInput(clienteId, null, null, "XYZ9876", RELATO)))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Veículo não encontrado para a placa informada.");
        assertThat(repository.ordens).isEmpty();
    }

    @Test
    void veiculoDeOutroClienteViolaRegraDeNegocio() {
        UUID outroCliente = cadastro.cliente("11144477735");

        assertThatThrownBy(() -> useCase.execute(new CriarOrdemServicoInput(outroCliente, veiculoId, RELATO)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("O veículo informado não pertence ao cliente.");
        assertThat(repository.ordens).isEmpty();
    }
}
