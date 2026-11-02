package br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.usecase;

import br.com.fiap.oficina_mecanica.compartilhado.domain.exception.RegraNegocioException;

/**
 * Lançada quando tentam aprovar um orçamento vencido. A expiração e a recusa da OS precisam ser gravadas
 * mesmo com o erro, então só esta exceção não desfaz a transação.
 */
public class OrcamentoExpiradoException extends RegraNegocioException {

    public OrcamentoExpiradoException() {
        super("O orçamento está expirado e não pode ser aprovado.");
    }
}
