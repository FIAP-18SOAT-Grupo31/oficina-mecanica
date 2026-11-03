package br.com.fiap.oficina_mecanica.veiculo.application.usecase;

import br.com.fiap.oficina_mecanica.compartilhado.application.dto.PaginaOutput;
import br.com.fiap.oficina_mecanica.veiculo.application.dto.ListarVeiculosInput;
import br.com.fiap.oficina_mecanica.veiculo.application.dto.VeiculoOutput;
import br.com.fiap.oficina_mecanica.veiculo.domain.VeiculoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListarVeiculosUseCase {

    private final VeiculoRepository repository;

    public ListarVeiculosUseCase(VeiculoRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public PaginaOutput<VeiculoOutput> execute(ListarVeiculosInput input) {
        return PaginaOutput.de(
                repository.listar(
                        input.clienteId(),
                        normalizarPlaca(input.placa()),
                        normalizarModelo(input.modelo()),
                        input.pagina(),
                        input.tamanho()
                ).mapear(VeiculoOutput::de)
        );
    }

    private static String normalizarPlaca(String placa) {
        if (placa == null || placa.isBlank()) {
            return null;
        }
        return placa.replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
    }

    private static String normalizarModelo(String modelo) {
        if (modelo == null || modelo.isBlank()) {
            return null;
        }
        return modelo.trim();
    }
}
