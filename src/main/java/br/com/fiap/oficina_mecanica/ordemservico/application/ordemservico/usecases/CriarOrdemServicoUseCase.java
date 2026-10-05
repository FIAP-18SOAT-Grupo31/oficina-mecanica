package br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.usecases;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.github.f4b6a3.uuid.UuidCreator;

import br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.dtos.CriarOrdemServicoInput;
import br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.dtos.CriarOrdemServicoOutput;
import br.com.fiap.oficina_mecanica.ordemservico.application.services.ClienteService;
import br.com.fiap.oficina_mecanica.ordemservico.application.services.VeiculoService;
import br.com.fiap.oficina_mecanica.ordemservico.domain.ordemservico.OrdemServico;
import br.com.fiap.oficina_mecanica.ordemservico.domain.ordemservico.OrdemServicoRepository;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class CriarOrdemServicoUseCase {
    
    private final ClienteService clienteService;
    private final VeiculoService veiculoService;
    private final OrdemServicoRepository ordemServicoRepository;

    public CriarOrdemServicoOutput execute(CriarOrdemServicoInput input) {
        
        if (!clienteService.clienteExistePorId(input.clienteId())) {
            throw new ValidationException("Cliente não encontrado no sistema");
        }

        if (!veiculoService.veiculoExistePorId(input.veiculoId())) {
            throw new ValidationException("Veículo não encontrado no sistema");
        }

        if (input.relatoProblema() == null || input.relatoProblema().isBlank()) {
            throw new ValidationException("Relato do problema não pode ser nulo ou vazio");
        }

        UUID ordemServicoId = UuidCreator.getTimeOrderedEpoch();

        OrdemServico ordemServico = new OrdemServico(ordemServicoId, input.clienteId(), input.veiculoId(), input.relatoProblema());
        var ordemServicoSalva = ordemServicoRepository.save(ordemServico);

        return new CriarOrdemServicoOutput(ordemServicoSalva.getId());
    }
}
