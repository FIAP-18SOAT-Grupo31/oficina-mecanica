package br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.usecase;

import java.util.UUID;

public interface VerificadorAcessoCliente {

    void validar(UUID ordemServicoId, String cpfInformado, String codigoAcesso);

}