package br.com.fiap.oficina_mecanica.ordemservico.infrastructure.ordemservico;

import br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.dto.ClienteDetalheOutput;
import br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.usecase.ConsultorClientes;
import br.com.fiap.oficina_mecanica.ordemservico.infrastructure.ordemservico.entity.OrdemServicoEntity;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class ResolvedorEmailCliente {

    private final OrdemServicoJpaRepository ordemServicoJpaRepository;
    private final ConsultorClientes consultorClientes;

    public ResolvedorEmailCliente(OrdemServicoJpaRepository ordemServicoJpaRepository,
                                  ConsultorClientes consultorClientes) {
        this.ordemServicoJpaRepository = ordemServicoJpaRepository;
        this.consultorClientes = consultorClientes;
    }

    public Optional<String> porOrdemServico(UUID ordemServicoId) {
        return ordemServicoJpaRepository.findById(ordemServicoId)
                .map(OrdemServicoEntity::getClienteId)
                .flatMap(consultorClientes::detalharPorId)
                .map(ClienteDetalheOutput::email);
    }
}
