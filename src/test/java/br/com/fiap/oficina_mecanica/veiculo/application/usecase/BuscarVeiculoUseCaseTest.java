package br.com.fiap.oficina_mecanica.veiculo.application.usecase;

import br.com.fiap.oficina_mecanica.compartilhado.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.oficina_mecanica.veiculo.application.dto.VeiculoOutput;
import br.com.fiap.oficina_mecanica.veiculo.domain.Veiculo;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BuscarVeiculoUseCaseTest {

    private final VeiculoRepositoryEmMemoria repository = new VeiculoRepositoryEmMemoria();
    private final BuscarVeiculoUseCase useCase = new BuscarVeiculoUseCase(repository);

    @Test
    void retornaOVeiculoEncontrado() {
        Veiculo veiculo = repository.salvar(VeiculoFixtures.veiculo());

        VeiculoOutput output = useCase.execute(veiculo.getId());

        assertThat(output.id()).isEqualTo(veiculo.getId());
        assertThat(output.placa()).isEqualTo("ABC1D23");
        assertThat(output.clienteId()).isEqualTo(VeiculoFixtures.CLIENTE_ID);
    }

    @Test
    void veiculoInexistenteViraRecursoNaoEncontrado() {
        assertThatThrownBy(() -> useCase.execute(UUID.randomUUID()))
                .isInstanceOf(VeiculoNaoEncontradoException.class)
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Veículo não encontrado.");
    }
}
