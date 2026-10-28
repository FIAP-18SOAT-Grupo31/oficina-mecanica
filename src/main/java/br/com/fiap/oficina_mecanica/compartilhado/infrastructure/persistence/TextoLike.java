package br.com.fiap.oficina_mecanica.compartilhado.infrastructure.persistence;

public final class TextoLike {

    public static final char ESCAPE = '\\';

    private TextoLike() {
    }

    /**
     * Monta o padrão "contém", sem diferenciar maiúsculas, tratando %, _ e \ como texto literal.
     */
    public static String contem(String texto) {
        String escapado = texto.toLowerCase()
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
        return "%" + escapado + "%";
    }
}
