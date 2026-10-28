package br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.dto;

import java.util.UUID;

public record VeiculoDetalheOutput(UUID id, String placa, String marca, String modelo, Integer ano) {}
