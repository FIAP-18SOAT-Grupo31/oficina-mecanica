package br.com.fiap.oficina_mecanica.compartilhado.application.dto;

import br.com.fiap.oficina_mecanica.compartilhado.domain.Pagina;

import java.util.List;

public record PaginaOutput<T>(List<T> conteudo, int pagina, int tamanho, long totalElementos, int totalPaginas) {

    public static <T> PaginaOutput<T> de(Pagina<T> pagina) {
        return new PaginaOutput<>(
                pagina.conteudo(),
                pagina.pagina(),
                pagina.tamanho(),
                pagina.totalElementos(),
                pagina.totalPaginas()
        );
    }
}
