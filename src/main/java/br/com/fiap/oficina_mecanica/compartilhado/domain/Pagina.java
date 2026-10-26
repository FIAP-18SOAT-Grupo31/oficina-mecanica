package br.com.fiap.oficina_mecanica.compartilhado.domain;

import java.util.List;
import java.util.function.Function;

public record Pagina<T>(List<T> conteudo, int pagina, int tamanho, long totalElementos, int totalPaginas) {

    public static <T> Pagina<T> de(List<T> conteudo, Paginacao paginacao, long totalElementos) {
        int totalPaginas = (int) Math.ceil((double) totalElementos / paginacao.tamanho());
        return new Pagina<>(List.copyOf(conteudo), paginacao.pagina(), paginacao.tamanho(), totalElementos, totalPaginas);
    }

    public <R> Pagina<R> mapear(Function<? super T, ? extends R> conversor) {
        List<R> convertido = conteudo.stream().<R>map(conversor).toList();
        return new Pagina<>(convertido, pagina, tamanho, totalElementos, totalPaginas);
    }
}
