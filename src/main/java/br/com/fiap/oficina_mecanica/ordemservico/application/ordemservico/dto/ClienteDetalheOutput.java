package br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.dto;

import java.util.UUID;

public record ClienteDetalheOutput(UUID id, String nome, String documento, String email, String telefone) {}
