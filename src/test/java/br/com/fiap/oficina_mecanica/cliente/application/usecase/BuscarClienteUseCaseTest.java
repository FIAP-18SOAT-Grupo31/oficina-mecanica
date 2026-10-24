package br.com.fiap.oficina_mecanica.cliente.application.usecase;

import br.com.fiap.oficina_mecanica.cliente.application.dto.ClienteOutput;
import br.com.fiap.oficina_mecanica.cliente.domain.Cliente;
import br.com.fiap.oficina_mecanica.compartilhado.domain.exception.RecursoNaoEncontradoException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BuscarClienteUseCaseTest {

    private final ClienteRepositoryEmMemoria repository = new ClienteRepositoryEmMemoria();
    private final BuscarClienteUseCase useCase = new BuscarClienteUseCase(repository);

    @Test
    void retornaOClienteEncontrado() {
        Cliente cliente = repository.salvar(ClienteFixtures.cliente());

        ClienteOutput output = useCase.execute(cliente.getId());

        assertThat(output.id()).isEqualTo(cliente.getId());
        assertThat(output.nome()).isEqualTo("Maria Silva");
        assertThat(output.documento()).isEqualTo("52998224725");
        assertThat(output.endereco().cidade()).isEqualTo("São Paulo");
    }

    @Test
    void clienteInexistenteViraRecursoNaoEncontrado() {
        assertThatThrownBy(() -> useCase.execute(UUID.randomUUID()))
                .isInstanceOf(ClienteNaoEncontradoException.class)
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Cliente não encontrado.");
    }
}
