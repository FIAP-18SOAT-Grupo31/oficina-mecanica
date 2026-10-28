package br.com.fiap.oficina_mecanica.cliente.application.usecase;

import br.com.fiap.oficina_mecanica.cliente.application.dto.ClienteOutput;
import br.com.fiap.oficina_mecanica.cliente.application.dto.CriarClienteInput;
import br.com.fiap.oficina_mecanica.cliente.application.dto.EnderecoInput;
import br.com.fiap.oficina_mecanica.cliente.domain.Estado;
import br.com.fiap.oficina_mecanica.compartilhado.domain.exception.RegraNegocioException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CriarClienteUseCaseTest {

    private final ClienteRepositoryEmMemoria repository = new ClienteRepositoryEmMemoria();
    private final CriarClienteUseCase useCase = new CriarClienteUseCase(repository);

    static Stream<Arguments> entradasInvalidas() {
        EnderecoInput endereco = ClienteFixtures.enderecoInput();
        EnderecoInput enderecoComCepInvalido =
                new EnderecoInput("Rua A", "1", null, "Centro", "Cidade", Estado.SP, "123");
        return Stream.of(
                Arguments.of(ClienteFixtures.criarInput("Maria", "529.982.247-24", "11987654321", "m@email.com", endereco),
                        "O documento do cliente é inválido"),
                Arguments.of(ClienteFixtures.criarInput("   ", ClienteFixtures.CPF, "11987654321", "m@email.com", endereco),
                        "O(a) nome do cliente é inválido(a)."),
                Arguments.of(ClienteFixtures.criarInput("Maria", ClienteFixtures.CPF, "123", "m@email.com", endereco),
                        "O telefone do cliente é inválido"),
                Arguments.of(ClienteFixtures.criarInput("Maria", ClienteFixtures.CPF, "11987654321", "sem-arroba", endereco),
                        "O e-mail do cliente é inválido"),
                Arguments.of(ClienteFixtures.criarInput("Maria", ClienteFixtures.CPF, "11987654321", "m@email.com", null),
                        "O endereço do cliente é inválido"),
                Arguments.of(ClienteFixtures.criarInput("Maria", ClienteFixtures.CPF, "11987654321", "m@email.com",
                                enderecoComCepInvalido),
                        "O CEP do endereço é inválido")
        );
    }

    @Test
    void criaClienteNormalizandoOsDadosEPersistindo() {
        ClienteOutput output = useCase.execute(ClienteFixtures.criarInput());

        assertThat(output.id()).isNotNull();
        assertThat(output.nome()).isEqualTo("Maria Silva");
        assertThat(output.documento()).isEqualTo("52998224725");
        assertThat(output.telefone()).isEqualTo("11987654321");
        assertThat(output.email()).isEqualTo("maria@email.com");
        assertThat(output.endereco().rua()).isEqualTo("Rua Das Flores");
        assertThat(output.endereco().numero()).isEqualTo("123");
        assertThat(output.endereco().complemento()).isNull();
        assertThat(output.endereco().bairro()).isEqualTo("Centro");
        assertThat(output.endereco().cidade()).isEqualTo("São Paulo");
        assertThat(output.endereco().estado()).isEqualTo(Estado.SP);
        assertThat(output.endereco().cep()).isEqualTo("01001000");
        assertThat(repository.buscarPorId(output.id())).isPresent();
    }

    @Test
    void recusaDocumentoJaCadastradoMesmoComMascaraDiferente() {
        useCase.execute(ClienteFixtures.criarInput("52998224725"));

        assertThatThrownBy(() -> useCase.execute(ClienteFixtures.criarInput("529.982.247-25")))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Já existe um cliente cadastrado com este documento.");
        assertThat(repository.clientes).hasSize(1);
    }

    @ParameterizedTest
    @MethodSource("entradasInvalidas")
    void converteDadoInvalidoDoDominioEmRegraDeNegocioSemPersistir(CriarClienteInput input, String mensagem) {
        assertThatThrownBy(() -> useCase.execute(input))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage(mensagem);
        assertThat(repository.clientes).isEmpty();
    }
}
