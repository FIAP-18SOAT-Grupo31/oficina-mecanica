package br.com.fiap.oficina_mecanica.veiculo.application.usecase;

import br.com.fiap.oficina_mecanica.compartilhado.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.oficina_mecanica.compartilhado.domain.exception.RegraNegocioException;
import br.com.fiap.oficina_mecanica.veiculo.application.dto.CriarVeiculoInput;
import br.com.fiap.oficina_mecanica.veiculo.application.dto.VeiculoOutput;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CriarVeiculoUseCaseTest {

    private final VeiculoRepositoryEmMemoria repository = new VeiculoRepositoryEmMemoria();
    private final CriarVeiculoUseCase useCase = new CriarVeiculoUseCase(repository, id -> true);

    static Stream<Arguments> entradasInvalidas() {
        UUID cliente = VeiculoFixtures.CLIENTE_ID;
        return Stream.of(
                Arguments.of(VeiculoFixtures.criarInput(cliente, "AB123", "Fiat", "Argo", 2022),
                        "A placa do veículo é inválida."),
                Arguments.of(VeiculoFixtures.criarInput(cliente, "ABC1D23", " ", "Argo", 2022),
                        "O(a) marca do veículo é inválido(a)"),
                Arguments.of(VeiculoFixtures.criarInput(cliente, "ABC1D23", "Fiat", " ", 2022),
                        "O(a) modelo do veículo é inválido(a)"),
                Arguments.of(VeiculoFixtures.criarInput(cliente, "ABC1D23", "Fiat", "Argo", 1899),
                        "Ano de fabricação do veículo é inválido."),
                Arguments.of(VeiculoFixtures.criarInput(cliente, "ABC1D23", "Fiat", "Argo", null),
                        "Ano de fabricação do veículo é inválido."),
                Arguments.of(VeiculoFixtures.criarInput(null, "ABC1D23", "Fiat", "Argo", 2022),
                        "O cliente do veículo é inválido.")
        );
    }

    @Test
    void criaVeiculoNormalizandoOsDadosEPersistindo() {
        VeiculoOutput output = useCase.execute(VeiculoFixtures.criarInput());

        assertThat(output.id()).isNotNull();
        assertThat(output.clienteId()).isEqualTo(VeiculoFixtures.CLIENTE_ID);
        assertThat(output.placa()).isEqualTo("ABC1D23");
        assertThat(output.marca()).isEqualTo("Fiat");
        assertThat(output.modelo()).isEqualTo("Argo");
        assertThat(output.anoFabricacao()).isEqualTo(2022);
        assertThat(repository.buscarPorId(output.id())).isPresent();
    }

    @Test
    void clienteInexistenteViraRecursoNaoEncontrado() {
        CriarVeiculoUseCase semCliente = new CriarVeiculoUseCase(repository, id -> false);

        assertThatThrownBy(() -> semCliente.execute(VeiculoFixtures.criarInput()))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Cliente não encontrado.");
        assertThat(repository.veiculos).isEmpty();
    }

    @Test
    void recusaPlacaJaCadastradaMesmoComMascaraDiferente() {
        useCase.execute(VeiculoFixtures.criarInput());

        assertThatThrownBy(() -> useCase.execute(
                VeiculoFixtures.criarInput(VeiculoFixtures.CLIENTE_ID, "ABC1D23", "Fiat", "Argo", 2022)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Já existe um veículo cadastrado com esta placa.");
        assertThat(repository.veiculos).hasSize(1);
    }

    @ParameterizedTest
    @MethodSource("entradasInvalidas")
    void converteDadoInvalidoDoDominioEmRegraDeNegocioSemPersistir(CriarVeiculoInput input, String mensagem) {
        assertThatThrownBy(() -> useCase.execute(input))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage(mensagem);
        assertThat(repository.veiculos).isEmpty();
    }
}
