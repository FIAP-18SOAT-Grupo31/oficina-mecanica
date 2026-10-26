package br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.usecase;

import br.com.fiap.oficina_mecanica.compartilhado.application.PublicadorEventos;
import br.com.fiap.oficina_mecanica.compartilhado.domain.Pagina;
import br.com.fiap.oficina_mecanica.compartilhado.domain.Paginacao;
import br.com.fiap.oficina_mecanica.compartilhado.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.dto.CriarOrdemServicoInput;
import br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.dto.CriarOrdemServicoOutput;
import br.com.fiap.oficina_mecanica.ordemservico.domain.evento.OrdemServicoCriada;
import br.com.fiap.oficina_mecanica.ordemservico.domain.ordemservico.OrdemServico;
import br.com.fiap.oficina_mecanica.ordemservico.domain.ordemservico.OrdemServicoRepository;
import br.com.fiap.oficina_mecanica.ordemservico.domain.ordemservico.StatusOrdemServico;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

        @Override
        public Pagina<OrdemServico> listar(StatusOrdemServico status, Paginacao paginacao) {
            throw new UnsupportedOperationException();
        }
    }

    private final OrdemServicoRepositoryEmMemoria repository = new OrdemServicoRepositoryEmMemoria();
    private final List<Object> eventos = new ArrayList<>();
    private final PublicadorEventos publicador = eventos::add;
    private final CriarOrdemServicoInput input =
            new CriarOrdemServicoInput(UUID.randomUUID(), UUID.randomUUID(), "Barulho na suspensão dianteira");

    @Test
    void criaOrdemRecebidaParaClienteEVeiculoExistentes() {
        CriarOrdemServicoUseCase useCase = new CriarOrdemServicoUseCase(id -> true, id -> true, repository, publicador);

        CriarOrdemServicoOutput output = useCase.execute(input);

        OrdemServico salva = repository.buscarPorId(output.ordemServicoId()).orElseThrow();
        assertThat(salva.getClienteId()).isEqualTo(input.clienteId());
        assertThat(salva.getVeiculoId()).isEqualTo(input.veiculoId());
        assertThat(salva.getStatus()).isEqualTo(StatusOrdemServico.RECEBIDA);
        assertThat(eventos).containsExactly(new OrdemServicoCriada(salva.getId(), salva.getCodigoAcesso()));
    }

    @Test
    void clienteInexistenteViraRecursoNaoEncontrado() {
        CriarOrdemServicoUseCase useCase = new CriarOrdemServicoUseCase(id -> false, id -> true, repository, publicador);

        assertThatThrownBy(() -> useCase.execute(input))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Cliente não encontrado.");
        assertThat(repository.ordens).isEmpty();
        assertThat(eventos).isEmpty();
    }

    @Test
    void veiculoInexistenteViraRecursoNaoEncontrado() {
        CriarOrdemServicoUseCase useCase = new CriarOrdemServicoUseCase(id -> true, id -> false, repository, publicador);

        assertThatThrownBy(() -> useCase.execute(input))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Veículo não encontrado.");
        assertThat(repository.ordens).isEmpty();
        assertThat(eventos).isEmpty();
    }
}
