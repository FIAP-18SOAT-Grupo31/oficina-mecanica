package br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.usecase;

import br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.dto.TempoExecucaoMedioOutput;
import br.com.fiap.oficina_mecanica.ordemservico.domain.ordemservico.OrdemServicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConsultarTempoExecucaoMedioUseCase {

    private final OrdemServicoRepository repository;

    public ConsultarTempoExecucaoMedioUseCase(OrdemServicoRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public TempoExecucaoMedioOutput execute() {
        return TempoExecucaoMedioOutput.de(repository.calcularTempoExecucaoMedio());
    }
}
