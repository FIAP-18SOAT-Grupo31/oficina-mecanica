package br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CriarOrdemServicoInput(
        @Schema(description = "Id do cliente. Use este campo ou documentoCliente.") UUID clienteId,
        @Schema(description = "CPF ou CNPJ do cliente, com ou sem pontuação. Use este campo ou clienteId.",
                example = "529.982.247-25") String documentoCliente,
        @Schema(description = "Id do veículo. Use este campo ou placaVeiculo.") UUID veiculoId,
        @Schema(description = "Placa do veículo, padrão antigo ou Mercosul. Use este campo ou veiculoId.",
                example = "ABC1D23") String placaVeiculo,
        @NotBlank(message = "é obrigatório")
        @Size(min = 10, max = 500, message = "deve ter entre 10 e 500 caracteres") String relatoProblema
) {

    public CriarOrdemServicoInput(UUID clienteId, UUID veiculoId, String relatoProblema) {
        this(clienteId, null, veiculoId, null, relatoProblema);
    }

    @Schema(hidden = true)
    @AssertTrue(message = "informe clienteId ou documentoCliente, apenas um deles")
    public boolean isClienteIdentificado() {
        return (clienteId != null) != informado(documentoCliente);
    }

    @Schema(hidden = true)
    @AssertTrue(message = "informe veiculoId ou placaVeiculo, apenas um deles")
    public boolean isVeiculoIdentificado() {
        return (veiculoId != null) != informado(placaVeiculo);
    }

    private static boolean informado(String valor) {
        return valor != null && !valor.isBlank();
    }
}
