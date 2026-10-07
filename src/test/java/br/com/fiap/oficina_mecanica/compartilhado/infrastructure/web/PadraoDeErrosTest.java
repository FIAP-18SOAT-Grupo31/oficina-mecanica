package br.com.fiap.oficina_mecanica.compartilhado.infrastructure.web;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import jakarta.validation.ValidationException;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "br.com.fiap.oficina_mecanica", importOptions = ImportOption.DoNotIncludeTests.class)
class PadraoDeErrosTest {

    @ArchTest
    static final ArchRule exceptionHandlerSoNoTratadorGlobal = methods()
            .that().areAnnotatedWith(ExceptionHandler.class)
            .should().beDeclaredIn(TratadorGlobalDeExcecoes.class)
            .because("todo erro HTTP sai pelo TratadorGlobalDeExcecoes");

    @ArchTest
    static final ArchRule umUnicoTratadorGlobal = classes()
            .that().areAnnotatedWith(ControllerAdvice.class)
            .or().areAnnotatedWith(RestControllerAdvice.class)
            .should().be(TratadorGlobalDeExcecoes.class)
            .because("todo erro HTTP sai pelo TratadorGlobalDeExcecoes");

    @ArchTest
    static final ArchRule corpoDeErroEhErroResposta = noClasses()
            .should().dependOnClassesThat().areAssignableTo(ProblemDetail.class)
            .because("o corpo de erro da aplicação é o ErroResposta");

    @ArchTest
    static final ArchRule semValidationExceptionDoFramework = noClasses()
            .should().dependOnClassesThat().areAssignableTo(ValidationException.class)
            .because("não encontrado é RecursoNaoEncontradoException, regra de negócio é RegraNegocioException "
                    + "e validação de entrada é Bean Validation com @Valid");

    @ArchTest
    static final ArchRule excecoesDeDominioSemFramework = noClasses()
            .that().resideInAPackage("..compartilhado.domain.exception..")
            .should().dependOnClassesThat().resideInAnyPackage("org.springframework..", "jakarta..")
            .because("as exceções de domínio não dependem de framework");
}
