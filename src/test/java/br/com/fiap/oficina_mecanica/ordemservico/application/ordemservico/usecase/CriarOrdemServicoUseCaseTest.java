package br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.usecase;

import br.com.fiap.oficina_mecanica.compartilhado.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.dto.CriarOrdemServicoInput;
import br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.dto.CriarOrdemServicoOutput;
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
    }

    private final OrdemServicoRepositoryEmMemoria repository = new OrdemServicoRepositoryEmMemoria();
    private final CriarOrdemServicoInput input =
            new CriarOrdemServicoInput(UUID.randomUUID(), UUID.randomUUID(), "Barulho na suspensão dianteira");

    @Test
    void criaOrdemRecebidaParaClienteEVeiculoExistentes() {
        CriarOrdemServicoUseCase useCase = new CriarOrdemServicoUseCase(id -> true, id -> true, repository);

        CriarOrdemServicoOutput output = useCase.execute(input);

        OrdemServico salva = repository.buscarPorId(output.ordemServicoId()).orElseThrow();
        assertThat(salva.getClienteId()).isEqualTo(input.clienteId());
        assertThat(salva.getVeiculoId()).isEqualTo(input.veiculoId());
        assertThat(salva.getStatus()).isEqualTo(StatusOrdemServico.RECEBIDA);
    }

    @Test
    void clienteInexistenteViraRecursoNaoEncontrado() {
        CriarOrdemServicoUseCase useCase = new CriarOrdemServicoUseCase(id -> false, id -> true, repository);

        assertThatThrownBy(() -> useCase.execute(input))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Cliente não encontrado.");
        assertThat(repository.ordens).isEmpty();
    }

    @Test
    void veiculoInexistenteViraRecursoNaoEncontrado() {
        CriarOrdemServicoUseCase useCase = new CriarOrdemServicoUseCase(id -> true, id -> false, repository);

        assertThatThrownBy(() -> useCase.execute(input))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Veículo não encontrado.");
        assertThat(repository.ordens).isEmpty();
    }
}
