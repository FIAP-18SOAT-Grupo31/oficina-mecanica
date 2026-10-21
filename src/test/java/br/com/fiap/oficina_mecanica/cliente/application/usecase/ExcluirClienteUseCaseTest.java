package br.com.fiap.oficina_mecanica.cliente.application.usecase;

import br.com.fiap.oficina_mecanica.cliente.domain.Cliente;
import br.com.fiap.oficina_mecanica.compartilhado.domain.exception.RegraNegocioException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExcluirClienteUseCaseTest {

    private final ClienteRepositoryEmMemoria repository = new ClienteRepositoryEmMemoria();

    @Test
    void excluiClienteSemVeiculos() {
        Cliente cliente = repository.salvar(ClienteFixtures.cliente());
        ExcluirClienteUseCase useCase = new ExcluirClienteUseCase(repository, id -> false);

        useCase.execute(cliente.getId());

        assertThat(repository.clientes).isEmpty();
    }

    @Test
    void clienteInexistenteViraRecursoNaoEncontradoSemConsultarVeiculos() {
        ExcluirClienteUseCase useCase = new ExcluirClienteUseCase(repository, id -> {
            throw new AssertionError("Não deveria consultar veículos de um cliente inexistente");
        });

        assertThatThrownBy(() -> useCase.execute(UUID.randomUUID()))
                .isInstanceOf(ClienteNaoEncontradoException.class)
                .hasMessage("Cliente não encontrado.");
    }

    @Test
    void recusaExcluirClienteComVeiculosVinculados() {
        Cliente cliente = repository.salvar(ClienteFixtures.cliente());
        ExcluirClienteUseCase useCase = new ExcluirClienteUseCase(repository, id -> id.equals(cliente.getId()));

        assertThatThrownBy(() -> useCase.execute(cliente.getId()))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Não é possível excluir o cliente: existem veículos vinculados a ele.");
        assertThat(repository.clientes).containsKey(cliente.getId());
    }
}
