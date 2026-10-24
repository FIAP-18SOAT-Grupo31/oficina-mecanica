package br.com.fiap.oficina_mecanica.veiculo.application.usecase;

import br.com.fiap.oficina_mecanica.veiculo.domain.Veiculo;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExcluirVeiculoUseCaseTest {

    private final VeiculoRepositoryEmMemoria repository = new VeiculoRepositoryEmMemoria();
    private final ExcluirVeiculoUseCase useCase = new ExcluirVeiculoUseCase(repository);

    @Test
    void excluiOVeiculo() {
        Veiculo veiculo = repository.salvar(VeiculoFixtures.veiculo());

        useCase.execute(veiculo.getId());

        assertThat(repository.veiculos).isEmpty();
    }

    @Test
    void veiculoInexistenteViraRecursoNaoEncontrado() {
        assertThatThrownBy(() -> useCase.execute(UUID.randomUUID()))
                .isInstanceOf(VeiculoNaoEncontradoException.class)
                .hasMessage("Veículo não encontrado.");
    }
}
