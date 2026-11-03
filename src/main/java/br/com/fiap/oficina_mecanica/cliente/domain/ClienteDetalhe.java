package br.com.fiap.oficina_mecanica.cliente.domain;

import java.util.UUID;

public record ClienteDetalhe(UUID id, String nome, String documento, String email, String telefone) {}
