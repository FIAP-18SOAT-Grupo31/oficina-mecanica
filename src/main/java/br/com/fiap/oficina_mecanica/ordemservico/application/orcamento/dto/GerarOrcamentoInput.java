package br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record GerarOrcamentoInput(
        @NotNull(message = "O ID da Ordem de Serviço é obrigatório.")
        UUID ordemServicoId,
        @NotNull(message = "A data de validade é obrigatória.")
        LocalDateTime dataValidade,
        @Valid
        List<ItemServicoInput> servicos,
        @Valid
        List<ItemPecaInput> pecas
) {
}