package br.com.fiap.oficina_mecanica.compartilhado.infrastructure.web;

import org.springframework.http.HttpStatus;

public enum TipoErro {

    DADOS_INVALIDOS(HttpStatus.BAD_REQUEST, "Dados Inválidos"),
    REQUISICAO_INVALIDA(HttpStatus.BAD_REQUEST, "Requisição Inválida"),
    NAO_AUTENTICADO(HttpStatus.UNAUTHORIZED, "Não Autenticado"),
    ACESSO_NEGADO(HttpStatus.FORBIDDEN, "Acesso Negado"),
    RECURSO_NAO_ENCONTRADO(HttpStatus.NOT_FOUND, "Recurso Não Encontrado"),
    METODO_NAO_PERMITIDO(HttpStatus.METHOD_NOT_ALLOWED, "Método Não Permitido"),
    CONFLITO_DE_DADOS(HttpStatus.CONFLICT, "Conflito de Dados"),
    FORMATO_NAO_SUPORTADO(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Formato Não Suportado"),
    REGRA_DE_NEGOCIO(HttpStatus.UNPROCESSABLE_CONTENT, "Violação de Regra de Negócio"),
    MUITAS_REQUISICOES(HttpStatus.TOO_MANY_REQUESTS, "Muitas Requisições"),
    ERRO_INTERNO(HttpStatus.INTERNAL_SERVER_ERROR, "Erro Interno do Servidor");

    private final HttpStatus status;
    private final String titulo;

    TipoErro(HttpStatus status, String titulo) {
        this.status = status;
        this.titulo = titulo;
    }

    public HttpStatus status() {
        return status;
    }

    public String titulo() {
        return titulo;
    }
}
