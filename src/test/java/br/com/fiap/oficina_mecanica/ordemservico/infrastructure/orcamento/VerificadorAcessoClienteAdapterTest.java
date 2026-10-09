package br.com.fiap.oficina_mecanica.ordemservico.infrastructure.orcamento;

import br.com.fiap.oficina_mecanica.cliente.domain.ClienteRepository;
import br.com.fiap.oficina_mecanica.compartilhado.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.oficina_mecanica.compartilhado.domain.exception.RegraNegocioException;
import br.com.fiap.oficina_mecanica.ordemservico.domain.ordemservico.StatusOrdemServico;
import br.com.fiap.oficina_mecanica.ordemservico.infrastructure.ordemservico.OrdemServicoJpaRepository;
import br.com.fiap.oficina_mecanica.ordemservico.infrastructure.ordemservico.entity.OrdemServicoEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VerificadorAcessoClienteAdapterTest {

    @Mock
    private OrdemServicoJpaRepository ordemServicoJpaRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private VerificadorAcessoClienteAdapter verificador;

    private OrdemServicoEntity criarOsEntity(UUID osId, UUID clienteId, String codigoAcesso) {
        return new OrdemServicoEntity(
                osId, clienteId, UUID.randomUUID(), "Problema", StatusOrdemServico.RECEBIDA,
                LocalDateTime.now(), null, codigoAcesso);
    }

    @Test
    void validaComSucessoQuandoCodigoECpfEstaoCorretos() {
        UUID osId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        String codigoCorreto = "ABC123";
        String cpfCorreto = "12345678900";

        when(ordemServicoJpaRepository.findById(osId))
                .thenReturn(Optional.of(criarOsEntity(osId, clienteId, codigoCorreto)));
        when(clienteRepository.buscarDocumentoPorId(clienteId))
                .thenReturn(Optional.of(cpfCorreto));

        assertThatCode(() -> verificador.validar(osId, cpfCorreto, codigoCorreto))
                .doesNotThrowAnyException();
    }

    @Test
    void lancaExcecaoQuandoOrdemServicoNaoExiste() {
        UUID osId = UUID.randomUUID();

        when(ordemServicoJpaRepository.findById(osId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> verificador.validar(osId, "123", "ABC"))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("Ordem de Serviço não encontrada");
    }

    @Test
    void lancaExcecaoQuandoCodigoAcessoIncorreto() {
        UUID osId = UUID.randomUUID();
        when(ordemServicoJpaRepository.findById(osId))
                .thenReturn(Optional.of(criarOsEntity(osId, UUID.randomUUID(), "ABC123")));

        assertThatThrownBy(() -> verificador.validar(osId, "123", "SENHA_ERRADA"))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("Credenciais inválidas");
    }

    @Test
    void lancaExcecaoQuandoClienteNaoEncontradoNoModuloDeCliente() {
        UUID osId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        String codigoCorreto = "ABC123";

        when(ordemServicoJpaRepository.findById(osId))
                .thenReturn(Optional.of(criarOsEntity(osId, clienteId, codigoCorreto)));
        when(clienteRepository.buscarDocumentoPorId(clienteId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> verificador.validar(osId, "123", codigoCorreto))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("Cliente não encontrado");
    }

    @Test
    void lancaExcecaoQuandoCpfIncorreto() {
        UUID osId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        String codigoCorreto = "ABC123";

        when(ordemServicoJpaRepository.findById(osId))
                .thenReturn(Optional.of(criarOsEntity(osId, clienteId, codigoCorreto)));
        when(clienteRepository.buscarDocumentoPorId(clienteId)).thenReturn(Optional.of("12345678900"));

        assertThatThrownBy(() -> verificador.validar(osId, "0000000", codigoCorreto))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("Credenciais inválidas");
    }
}