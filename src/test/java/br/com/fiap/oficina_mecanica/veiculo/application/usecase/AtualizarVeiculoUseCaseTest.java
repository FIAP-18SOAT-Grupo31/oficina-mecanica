package br.com.fiap.oficina_mecanica.veiculo.application.usecase;

import br.com.fiap.oficina_mecanica.compartilhado.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.oficina_mecanica.compartilhado.domain.exception.RegraNegocioException;
import br.com.fiap.oficina_mecanica.veiculo.application.dto.AtualizarVeiculoInput;
import br.com.fiap.oficina_mecanica.veiculo.application.dto.VeiculoOutput;
import br.com.fiap.oficina_mecanica.veiculo.domain.Veiculo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AtualizarVeiculoUseCaseTest {

    private final VeiculoRepositoryEmMemoria repository = new VeiculoRepositoryEmMemoria();
    private final AtualizarVeiculoUseCase useCase = new AtualizarVeiculoUseCase(repository, id -> true);

    static Stream<Arguments> entradasInvalidas() {
        UUID cliente = VeiculoFixtures.CLIENTE_ID;
        return Stream.of(
                Arguments.of(VeiculoFixtures.atualizarInput(cliente, "AB123", "Toyota", "Corolla", 2020),
                        "A placa do veículo é inválida."),
                Arguments.of(VeiculoFixtures.atualizarInput(cliente, "XYZ1D23", " ", "Corolla", 2020),
                        "O(a) marca do veículo é inválido(a)"),
                Arguments.of(VeiculoFixtures.atualizarInput(cliente, "XYZ1D23", "Toyota", "Corolla", null),
                        "Ano de fabricação do veículo é inválido."),
                Arguments.of(VeiculoFixtures.atualizarInput(null, "XYZ1D23", "Toyota", "Corolla", 2020),
                        "O cliente do veículo é inválido.")
        );
    }

    @Test
    void atualizaTodosOsDadosInclusiveOClienteDono() {
        Veiculo existente = repository.salvar(VeiculoFixtures.veiculo());
        UUID novoCliente = UUID.randomUUID();

        VeiculoOutput output = useCase.execute(existente.getId(),
                VeiculoFixtures.atualizarInput(novoCliente, "xyz-1d23", " Toyota ", " Corolla ", 2020));

        assertThat(output.id()).isEqualTo(existente.getId());
        assertThat(output.clienteId()).isEqualTo(novoCliente);
        assertThat(output.placa()).isEqualTo("XYZ1D23");
        assertThat(output.marca()).isEqualTo("Toyota");
        assertThat(output.modelo()).isEqualTo("Corolla");
        assertThat(output.anoFabricacao()).isEqualTo(2020);
    }

    @Test
    void permiteManterAPropriaPlaca() {
        Veiculo existente = repository.salvar(VeiculoFixtures.veiculo());

        VeiculoOutput output = useCase.execute(existente.getId(),
                VeiculoFixtures.atualizarInput(VeiculoFixtures.CLIENTE_ID, "ABC-1D23", "Fiat", "Argo", 2021));

        assertThat(output.placa()).isEqualTo("ABC1D23");
        assertThat(output.anoFabricacao()).isEqualTo(2021);
    }

    @Test
    void veiculoInexistenteViraRecursoNaoEncontrado() {
        assertThatThrownBy(() -> useCase.execute(UUID.randomUUID(),
                VeiculoFixtures.atualizarInput(VeiculoFixtures.CLIENTE_ID, "ABC1D23", "Fiat", "Argo", 2022)))
                .isInstanceOf(VeiculoNaoEncontradoException.class)
                .hasMessage("Veículo não encontrado.");
    }

    @Test
    void novoClienteInexistenteViraRecursoNaoEncontrado() {
        Veiculo existente = repository.salvar(VeiculoFixtures.veiculo());
        AtualizarVeiculoUseCase semCliente = new AtualizarVeiculoUseCase(repository, id -> false);

        assertThatThrownBy(() -> semCliente.execute(existente.getId(),
                VeiculoFixtures.atualizarInput(UUID.randomUUID(), "ABC1D23", "Fiat", "Argo", 2022)))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Cliente não encontrado.");
    }

    @Test
    void recusaPlacaQueJaPertenceAOutroVeiculo() {
        Veiculo existente = repository.salvar(VeiculoFixtures.veiculo());
        repository.salvar(VeiculoFixtures.veiculo("XYZ1D23"));

        assertThatThrownBy(() -> useCase.execute(existente.getId(),
                VeiculoFixtures.atualizarInput(VeiculoFixtures.CLIENTE_ID, "XYZ1D23", "Fiat", "Argo", 2022)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Já existe outro veículo cadastrado com esta placa.");
    }

    @ParameterizedTest
    @MethodSource("entradasInvalidas")
    void converteDadoInvalidoDoDominioEmRegraDeNegocioSemAlterarOVeiculo(AtualizarVeiculoInput input,
                                                                          String mensagem) {
        Veiculo existente = repository.salvar(VeiculoFixtures.veiculo());

        assertThatThrownBy(() -> useCase.execute(existente.getId(), input))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage(mensagem);

        Veiculo armazenado = repository.buscarPorId(existente.getId()).orElseThrow();
        assertThat(armazenado.getPlaca()).isEqualTo("ABC1D23");
        assertThat(armazenado.getMarca()).isEqualTo("Fiat");
        assertThat(armazenado.getModelo()).isEqualTo("Argo");
        assertThat(armazenado.getAnoFabricacao()).isEqualTo(2022);
        assertThat(armazenado.getClienteId()).isEqualTo(VeiculoFixtures.CLIENTE_ID);
    }
}
