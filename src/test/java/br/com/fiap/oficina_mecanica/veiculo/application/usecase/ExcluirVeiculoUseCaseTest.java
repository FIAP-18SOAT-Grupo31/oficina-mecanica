package br.com.fiap.oficina_mecanica.veiculo.application.usecase;

import br.com.fiap.oficina_mecanica.compartilhado.domain.exception.RegraNegocioException;
import br.com.fiap.oficina_mecanica.veiculo.domain.Veiculo;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExcluirVeiculoUseCaseTest {

    private final VeiculoRepositoryEmMemoria repository = new VeiculoRepositoryEmMemoria();
    private final Set<UUID> veiculosComOrdemServico = new HashSet<>();
    private final ExcluirVeiculoUseCase useCase =
            new ExcluirVeiculoUseCase(repository, veiculosComOrdemServico::contains);

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

    @Test
    void veiculoComOrdemServicoNaoPodeSerExcluido() {
        Veiculo veiculo = repository.salvar(VeiculoFixtures.veiculo());
        veiculosComOrdemServico.add(veiculo.getId());

        assertThatThrownBy(() -> useCase.execute(veiculo.getId()))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Não é possível excluir o veículo: existem ordens de serviço vinculadas a ele.");

        assertThat(repository.veiculos).hasSize(1);
    }

    @Test
    void veiculoInexistenteNaoConsultaAsOrdensDeServico() {
        UUID id = UUID.randomUUID();
        veiculosComOrdemServico.add(id);

        assertThatThrownBy(() -> useCase.execute(id)).isInstanceOf(VeiculoNaoEncontradoException.class);
    }
}
