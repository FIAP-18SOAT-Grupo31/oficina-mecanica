package br.com.fiap.oficina_mecanica.compartilhado.domain;

import java.util.List;
import java.util.function.Function;

public record Pagina<T>(List<T> conteudo, int pagina, int tamanho, long totalElementos) {

    public Pagina {
        if (conteudo == null) {
            throw new IllegalArgumentException("O conteúdo da página é inválido.");
        }
        if (pagina < 0) {
            throw new IllegalArgumentException("O número da página não pode ser negativo.");
        }
        if (tamanho < 1) {
            throw new IllegalArgumentException("O tamanho da página deve ser maior que zero.");
        }
        if (totalElementos < 0) {
            throw new IllegalArgumentException("O total de elementos não pode ser negativo.");
        }
        conteudo = List.copyOf(conteudo);
    }

    public static <T> Pagina<T> de(List<T> conteudo, Paginacao paginacao, long totalElementos) {
        return new Pagina<>(conteudo, paginacao.pagina(), paginacao.tamanho(), totalElementos);
    }

    public int totalPaginas() {
        return (int) ((totalElementos + tamanho - 1) / tamanho);
    }

    public <R> Pagina<R> mapear(Function<? super T, ? extends R> conversor) {
        List<R> convertido = conteudo.stream().<R>map(conversor).toList();
        return new Pagina<>(convertido, pagina, tamanho, totalElementos);
    }
}
