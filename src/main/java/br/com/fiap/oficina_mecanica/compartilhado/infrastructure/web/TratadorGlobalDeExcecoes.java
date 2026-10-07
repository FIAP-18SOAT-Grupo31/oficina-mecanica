package br.com.fiap.oficina_mecanica.compartilhado.infrastructure.web;

import br.com.fiap.oficina_mecanica.compartilhado.application.exception.NaoAutenticadoException;
import br.com.fiap.oficina_mecanica.compartilhado.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.oficina_mecanica.compartilhado.domain.exception.RegraNegocioException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Comparator;
import java.util.stream.Collectors;

@RestControllerAdvice
public class TratadorGlobalDeExcecoes {

    private static final Logger LOG = LoggerFactory.getLogger(TratadorGlobalDeExcecoes.class);

    @ExceptionHandler({RecursoNaoEncontradoException.class, IllegalArgumentException.class})
    public ResponseEntity<ErroResposta> tratarRecursoNaoEncontrado(RuntimeException ex) {
        return responder(TipoErro.RECURSO_NAO_ENCONTRADO, ex.getMessage());
    }

    @ExceptionHandler({RegraNegocioException.class, IllegalStateException.class})
    public ResponseEntity<ErroResposta> tratarRegraDeNegocio(RuntimeException ex) {
        return responder(TipoErro.REGRA_DE_NEGOCIO, ex.getMessage());
    }

    @ExceptionHandler(NaoAutenticadoException.class)
    public ResponseEntity<ErroResposta> tratarNaoAutenticado(NaoAutenticadoException ex) {
        return responder(TipoErro.NAO_AUTENTICADO, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResposta> tratarCamposInvalidos(MethodArgumentNotValidException ex) {
        String campos = ex.getBindingResult().getFieldErrors().stream()
                .sorted(Comparator.comparing(FieldError::getField))
                .map(campo -> campo.getField() + ": " + campo.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return responder(TipoErro.DADOS_INVALIDOS, campos);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErroResposta> tratarParametrosInvalidos(HandlerMethodValidationException ex) {
        String parametros = ex.getParameterValidationResults().stream()
                .flatMap(resultado -> resultado.getResolvableErrors().stream()
                        .map(erro -> resultado.getMethodParameter().getParameterName() + ": " + erro.getDefaultMessage()))
                .sorted()
                .collect(Collectors.joining("; "));
        return responder(TipoErro.DADOS_INVALIDOS, parametros);
    }

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class
    })
    public ResponseEntity<ErroResposta> tratarRequisicaoInvalida(Exception ex) {
        return responder(TipoErro.REQUISICAO_INVALIDA, "A requisição está mal formada ou tem parâmetros inválidos.");
    }

    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    public ResponseEntity<ErroResposta> tratarCaminhoInexistente(Exception ex) {
        return responder(TipoErro.RECURSO_NAO_ENCONTRADO, "O caminho informado não existe.");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErroResposta> tratarMetodoNaoPermitido(HttpRequestMethodNotSupportedException ex) {
        return responder(TipoErro.METODO_NAO_PERMITIDO, "O método " + ex.getMethod() + " não é aceito neste caminho.");
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErroResposta> tratarFormatoNaoSuportado(HttpMediaTypeNotSupportedException ex) {
        return responder(TipoErro.FORMATO_NAO_SUPORTADO, "Envie o corpo da requisição em JSON.");
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ErroResposta> tratarAlteracaoConcorrente(OptimisticLockingFailureException ex) {
        return responder(TipoErro.CONFLITO_DE_DADOS, "O registro foi alterado por outra operação. Tente novamente.");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroResposta> tratarViolacaoDeIntegridade(DataIntegrityViolationException ex) {
        LOG.warn("Violação de integridade: {}", ex.getMostSpecificCause().getClass().getSimpleName());
        return responder(TipoErro.CONFLITO_DE_DADOS, "Os dados conflitam com um registro existente ou em uso.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResposta> tratarErroInesperado(Exception ex) {
        LOG.error("Erro inesperado", ex);
        return responder(TipoErro.ERRO_INTERNO, "Ocorreu um erro inesperado. Contate o suporte.");
    }

    private static ResponseEntity<ErroResposta> responder(TipoErro tipo, String mensagem) {
        return ResponseEntity.status(tipo.status()).body(ErroResposta.de(tipo, mensagem));
    }
}
