package br.com.fiap.oficina_mecanica.cliente.application.dto;

public record ListarClientesInput(String nome, String documento, int pagina, int tamanho) {}
