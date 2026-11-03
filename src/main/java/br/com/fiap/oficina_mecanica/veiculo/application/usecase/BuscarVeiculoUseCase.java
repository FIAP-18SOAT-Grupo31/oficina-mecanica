package br.com.fiap.oficina_mecanica.veiculo.application.usecase;

import br.com.fiap.oficina_mecanica.veiculo.application.dto.VeiculoOutput;
import br.com.fiap.oficina_mecanica.veiculo.domain.VeiculoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class BuscarVeiculoUseCase {

    private final VeiculoRepository repository;

    public BuscarVeiculoUseCase(VeiculoRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public VeiculoOutput execute(UUID id) {
        return repository.buscarPorId(id)
                .map(VeiculoOutput::de)
                .orElseThrow(VeiculoNaoEncontradoException::new);
    }
}
