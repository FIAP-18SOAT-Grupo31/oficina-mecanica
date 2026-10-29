package br.com.fiap.oficina_mecanica.veiculo.application.dto;

import java.util.UUID;

public record ListarVeiculosInput(UUID clienteId, String placa, String modelo, int pagina, int tamanho) {}
