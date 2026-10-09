package br.com.fiap.oficina_mecanica.ordemservico.infrastructure.orcamento;

import br.com.fiap.oficina_mecanica.cliente.domain.ClienteRepository;
import br.com.fiap.oficina_mecanica.compartilhado.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.oficina_mecanica.compartilhado.domain.exception.RegraNegocioException;
import br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.usecase.VerificadorAcessoCliente;
import br.com.fiap.oficina_mecanica.ordemservico.infrastructure.ordemservico.OrdemServicoJpaRepository;
import br.com.fiap.oficina_mecanica.ordemservico.infrastructure.ordemservico.entity.OrdemServicoEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class VerificadorAcessoClienteAdapter implements VerificadorAcessoCliente {

    private final OrdemServicoJpaRepository ordemServicoJpaRepository;
    private final ClienteRepository clienteRepository;

    public VerificadorAcessoClienteAdapter(OrdemServicoJpaRepository ordemServicoJpaRepository, ClienteRepository clienteRepository) {
        this.ordemServicoJpaRepository = ordemServicoJpaRepository;
        this.clienteRepository = clienteRepository;
    }

    @Override
    public void validar(UUID ordemServicoId, String cpfInformado, String codigoAcesso) {
        OrdemServicoEntity os = ordemServicoJpaRepository.findById(ordemServicoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ordem de Serviço não encontrada."));

        if (!os.getCodigoAcesso().equals(codigoAcesso)) {
            throw new RegraNegocioException("Credenciais inválidas");
        }

        String cpfReal = clienteRepository.buscarDocumentoPorId(os.getClienteId())
                .orElseThrow(() -> new RegraNegocioException("Cliente não encontrado."));

        if (!cpfReal.equals(cpfInformado)) {
            throw new RegraNegocioException("Credenciais inválidas");
        }
    }
}