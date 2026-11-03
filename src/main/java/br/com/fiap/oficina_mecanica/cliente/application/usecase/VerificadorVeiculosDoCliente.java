package br.com.fiap.oficina_mecanica.cliente.application.usecase;

import java.util.UUID;

public interface VerificadorVeiculosDoCliente {
    boolean possuiVeiculos(UUID clienteId);
}
