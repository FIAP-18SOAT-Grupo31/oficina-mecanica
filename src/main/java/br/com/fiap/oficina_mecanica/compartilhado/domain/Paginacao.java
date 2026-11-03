package br.com.fiap.oficina_mecanica.compartilhado.domain;

public record Paginacao(int pagina, int tamanho) {

    public static final int TAMANHO_MAXIMO = 100;

    public Paginacao {
        if (pagina < 0) {
            throw new IllegalArgumentException("A página não pode ser negativa.");
        }
        if (tamanho < 1 || tamanho > TAMANHO_MAXIMO) {
            throw new IllegalArgumentException("O tamanho da página deve estar entre 1 e " + TAMANHO_MAXIMO + ".");
        }
    }
}
