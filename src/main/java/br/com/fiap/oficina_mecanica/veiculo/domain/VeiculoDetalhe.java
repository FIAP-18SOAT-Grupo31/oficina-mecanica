package br.com.fiap.oficina_mecanica.veiculo.domain;

import java.util.UUID;

public record VeiculoDetalhe(UUID id, String placa, String marca, String modelo, Integer ano) {}
