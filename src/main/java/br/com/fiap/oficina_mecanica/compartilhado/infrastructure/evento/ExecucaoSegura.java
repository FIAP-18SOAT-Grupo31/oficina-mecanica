package br.com.fiap.oficina_mecanica.compartilhado.infrastructure.evento;

import org.slf4j.Logger;

public final class ExecucaoSegura {

    private ExecucaoSegura() {
    }

    public static void executar(Logger log, String descricao, Runnable acao) {
        try {
            acao.run();
        } catch (RuntimeException ex) {
            log.error("Falha ao processar {}; a operação já foi concluída.", descricao, ex);
        }
    }
}
