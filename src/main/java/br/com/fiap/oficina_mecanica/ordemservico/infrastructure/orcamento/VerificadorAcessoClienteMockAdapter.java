package br.com.fiap.oficina_mecanica.ordemservico.infrastructure.orcamento;

import br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.usecase.VerificadorAcessoCliente;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class VerificadorAcessoClienteMockAdapter implements VerificadorAcessoCliente {

    @Override
    public void validar(UUID ordemServicoId, String cpfInformado, String codigoAcesso) {
        // Implementação temporária para o Spring subir
        // Implementar a lógica real que vai no banco de dados conferir a OS
        System.out.println("[MOCK] Validando acesso do cliente - CPF: " + cpfInformado + " | Código: " + codigoAcesso);
    }
}