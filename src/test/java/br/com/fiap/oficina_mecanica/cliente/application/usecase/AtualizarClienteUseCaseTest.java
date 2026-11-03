package br.com.fiap.oficina_mecanica.cliente.application.usecase;

import br.com.fiap.oficina_mecanica.cliente.application.dto.AtualizarClienteInput;
import br.com.fiap.oficina_mecanica.cliente.application.dto.ClienteOutput;
import br.com.fiap.oficina_mecanica.cliente.application.dto.EnderecoInput;
import br.com.fiap.oficina_mecanica.cliente.domain.Cliente;
import br.com.fiap.oficina_mecanica.cliente.domain.Estado;
import br.com.fiap.oficina_mecanica.compartilhado.domain.exception.RegraNegocioException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AtualizarClienteUseCaseTest {

    private final ClienteRepositoryEmMemoria repository = new ClienteRepositoryEmMemoria();
    private final AtualizarClienteUseCase useCase = new AtualizarClienteUseCase(repository);

    static Stream<Arguments> entradasInvalidas() {
        EnderecoInput endereco = ClienteFixtures.enderecoInput();
        EnderecoInput enderecoSemRua = new EnderecoInput(" ", "1", null, "Centro", "Cidade", Estado.SP, "01001000");
        return Stream.of(
                Arguments.of(ClienteFixtures.atualizarInput("Ana", "529.982.247-24", "11987654321", "a@email.com", endereco),
                        "O documento do cliente é inválido"),
                Arguments.of(ClienteFixtures.atualizarInput("   ", ClienteFixtures.OUTRO_CPF, "11987654321", "a@email.com",
                                endereco),
                        "O(a) nome do cliente é inválido(a)."),
                Arguments.of(ClienteFixtures.atualizarInput("Ana", ClienteFixtures.OUTRO_CPF, "123", "a@email.com", endereco),
                        "O telefone do cliente é inválido"),
                Arguments.of(ClienteFixtures.atualizarInput("Ana", ClienteFixtures.OUTRO_CPF, "11987654321", "sem-arroba",
                                endereco),
                        "O e-mail do cliente é inválido"),
                Arguments.of(ClienteFixtures.atualizarInput("Ana", ClienteFixtures.OUTRO_CPF, "11987654321", "a@email.com",
                                null),
                        "O endereço do cliente é inválido"),
                Arguments.of(ClienteFixtures.atualizarInput("Ana", ClienteFixtures.OUTRO_CPF, "11987654321", "a@email.com",
                                enderecoSemRua),
                        "O(a) rua do endereço é inválido(a)")
        );
    }

    @Test
    void atualizaTodosOsDadosNormalizandoEPersistindo() {
        Cliente existente = repository.salvar(ClienteFixtures.cliente());

        ClienteOutput output = useCase.execute(existente.getId(), ClienteFixtures.atualizarInput());

        assertThat(output.id()).isEqualTo(existente.getId());
        assertThat(output.nome()).isEqualTo("João Souza");
        assertThat(output.documento()).isEqualTo("00000003700");
        assertThat(output.telefone()).isEqualTo("2134567890");
        assertThat(output.email()).isEqualTo("joao@email.com");
        assertThat(output.endereco().rua()).isEqualTo("Avenida Brasil");
        assertThat(output.endereco().complemento()).isEqualTo("Bloco B");
        assertThat(output.endereco().bairro()).isEqualTo("Jardim América");
        assertThat(output.endereco().cidade()).isEqualTo("Rio De Janeiro");
        assertThat(output.endereco().estado()).isEqualTo(Estado.RJ);
        assertThat(output.endereco().cep()).isEqualTo("20000000");
        assertThat(repository.buscarPorId(existente.getId()).orElseThrow().getNome()).isEqualTo("João Souza");
    }

    @Test
    void permiteManterOProprioDocumento() {
        Cliente existente = repository.salvar(ClienteFixtures.cliente());
        AtualizarClienteInput input = ClienteFixtures.atualizarInput("Maria Souza", ClienteFixtures.CPF,
                "11987654321", "maria@email.com", ClienteFixtures.enderecoInput());

        ClienteOutput output = useCase.execute(existente.getId(), input);

        assertThat(output.documento()).isEqualTo("52998224725");
        assertThat(output.nome()).isEqualTo("Maria Souza");
    }

    @Test
    void clienteInexistenteViraRecursoNaoEncontrado() {
        assertThatThrownBy(() -> useCase.execute(UUID.randomUUID(), ClienteFixtures.atualizarInput()))
                .isInstanceOf(ClienteNaoEncontradoException.class)
                .hasMessage("Cliente não encontrado.");
    }

    @Test
    void recusaDocumentoQueJaPertenceAOutroCliente() {
        Cliente existente = repository.salvar(ClienteFixtures.cliente());
        repository.salvar(ClienteFixtures.cliente(ClienteFixtures.OUTRO_CPF));

        assertThatThrownBy(() -> useCase.execute(existente.getId(), ClienteFixtures.atualizarInput()))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Já existe outro cliente cadastrado com este documento.");
    }

    @ParameterizedTest
    @MethodSource("entradasInvalidas")
    void converteDadoInvalidoDoDominioEmRegraDeNegocioSemAlterarOCliente(AtualizarClienteInput input, String mensagem) {
        Cliente existente = repository.salvar(ClienteFixtures.cliente());

        assertThatThrownBy(() -> useCase.execute(existente.getId(), input))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage(mensagem);

        Cliente armazenado = repository.buscarPorId(existente.getId()).orElseThrow();
        assertThat(armazenado.getNome()).isEqualTo("Maria Silva");
        assertThat(armazenado.getDocumento()).isEqualTo("52998224725");
        assertThat(armazenado.getTelefone()).isEqualTo("11987654321");
        assertThat(armazenado.getEmail()).isEqualTo("maria@email.com");
        assertThat(armazenado.getEndereco().getRua()).isEqualTo("Rua Das Flores");
    }
}
