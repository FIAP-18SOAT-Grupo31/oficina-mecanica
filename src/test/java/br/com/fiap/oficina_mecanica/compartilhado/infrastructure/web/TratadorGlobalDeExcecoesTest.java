package br.com.fiap.oficina_mecanica.compartilhado.infrastructure.web;

import br.com.fiap.oficina_mecanica.compartilhado.application.exception.NaoAutenticadoException;
import br.com.fiap.oficina_mecanica.compartilhado.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.oficina_mecanica.compartilhado.domain.exception.RegraNegocioException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TratadorGlobalDeExcecoesTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ControllerDeTeste())
                .setControllerAdvice(new TratadorGlobalDeExcecoes())
                .build();
    }

    @Test
    void recursoNaoEncontradoVolta404ComAMensagemDaExcecao() throws Exception {
        confere(mockMvc.perform(get("/teste/recurso-nao-encontrado")), 404, "Recurso Não Encontrado")
                .andExpect(jsonPath("$.mensagem").value("Veículo não encontrado"));
    }

    @Test
    void illegalArgumentContinuaVoltando404() throws Exception {
        confere(mockMvc.perform(get("/teste/argumento-ilegal")), 404, "Recurso Não Encontrado")
                .andExpect(jsonPath("$.mensagem").value("Orçamento não encontrado"));
    }

    @Test
    void regraDeNegocioVolta422ComAMensagemDaExcecao() throws Exception {
        confere(mockMvc.perform(get("/teste/regra-de-negocio")), 422, "Violação de Regra de Negócio")
                .andExpect(jsonPath("$.mensagem").value("Estoque insuficiente"));
    }

    @Test
    void illegalStateContinuaVoltando422() throws Exception {
        confere(mockMvc.perform(get("/teste/estado-ilegal")), 422, "Violação de Regra de Negócio")
                .andExpect(jsonPath("$.mensagem").value("Orçamento já aprovado"));
    }

    @Test
    void naoAutenticadoVolta401ComAMensagemDaExcecao() throws Exception {
        confere(mockMvc.perform(get("/teste/nao-autenticado")), 401, "Não Autenticado")
                .andExpect(jsonPath("$.mensagem").value("Usuário ou senha inválidos."));
    }

    @Test
    void camposInvalidosVoltam400ComOsCamposEmOrdem() throws Exception {
        ResultActions resposta = mockMvc.perform(post("/teste/corpo")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"\",\"documento\":\"\"}"));

        confere(resposta, 400, "Dados Inválidos")
                .andExpect(jsonPath("$.mensagem").value("documento: é obrigatório; nome: é obrigatório"));
    }

    @Test
    void parametroForaDaRegraVolta400ComONomeDoParametro() throws Exception {
        confere(mockMvc.perform(get("/teste/pagina").param("numero", "0")), 400, "Dados Inválidos")
                .andExpect(jsonPath("$.mensagem").value("numero: deve ser a partir de 1"));
    }

    @Test
    void jsonMalFormadoVolta400() throws Exception {
        ResultActions resposta = mockMvc.perform(post("/teste/corpo")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":"));

        confere(resposta, 400, "Requisição Inválida");
    }

    @Test
    void parametroComTipoErradoVolta400() throws Exception {
        confere(mockMvc.perform(get("/teste/numero/abc")), 400, "Requisição Inválida");
    }

    @Test
    void parametroObrigatorioFaltandoVolta400() throws Exception {
        confere(mockMvc.perform(get("/teste/pagina")), 400, "Requisição Inválida");
    }

    @Test
    void caminhoInexistenteVolta404() throws Exception {
        confere(mockMvc.perform(get("/caminho/que/nao/existe")), 404, "Recurso Não Encontrado")
                .andExpect(jsonPath("$.mensagem").value("O caminho informado não existe."));
    }

    @Test
    void metodoNaoPermitidoVolta405() throws Exception {
        confere(mockMvc.perform(post("/teste/recurso-nao-encontrado")), 405, "Método Não Permitido")
                .andExpect(jsonPath("$.mensagem").value("O método POST não é aceito neste caminho."));
    }

    @Test
    void formatoNaoSuportadoVolta415() throws Exception {
        ResultActions resposta = mockMvc.perform(post("/teste/corpo")
                .contentType(MediaType.TEXT_PLAIN)
                .content("nome"));

        confere(resposta, 415, "Formato Não Suportado");
    }

    @Test
    void alteracaoConcorrenteVolta409() throws Exception {
        confere(mockMvc.perform(get("/teste/concorrencia")), 409, "Conflito de Dados")
                .andExpect(jsonPath("$.mensagem").value("O registro foi alterado por outra operação. Tente novamente."));
    }

    @Test
    void violacaoDeIntegridadeVolta409SemExporODetalheDoBanco() throws Exception {
        confere(mockMvc.perform(get("/teste/integridade")), 409, "Conflito de Dados")
                .andExpect(jsonPath("$.mensagem").value(not(containsString("uk_placa"))));
    }

    @Test
    void erroInesperadoVolta500SemExporAMensagemInterna() throws Exception {
        confere(mockMvc.perform(get("/teste/inesperado")), 500, "Erro Interno do Servidor")
                .andExpect(jsonPath("$.mensagem").value("Ocorreu um erro inesperado. Contate o suporte."));
    }

    private static ResultActions confere(ResultActions resposta, int status, String erro) throws Exception {
        return resposta
                .andExpect(status().is(status))
                .andExpect(jsonPath("$.status").value(status))
                .andExpect(jsonPath("$.erro").value(erro))
                .andExpect(jsonPath("$.mensagem", notNullValue()))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    record CorpoDeTeste(
            @NotBlank(message = "é obrigatório") String nome,
            @NotBlank(message = "é obrigatório") String documento
    ) {}

    @RestController
    @RequestMapping("/teste")
    static class ControllerDeTeste {

        @GetMapping("/recurso-nao-encontrado")
        void recursoNaoEncontrado() {
            throw new RecursoNaoEncontradoException("Veículo não encontrado");
        }

        @GetMapping("/argumento-ilegal")
        void argumentoIlegal() {
            throw new IllegalArgumentException("Orçamento não encontrado");
        }

        @GetMapping("/regra-de-negocio")
        void regraDeNegocio() {
            throw new RegraNegocioException("Estoque insuficiente");
        }

        @GetMapping("/estado-ilegal")
        void estadoIlegal() {
            throw new IllegalStateException("Orçamento já aprovado");
        }

        @GetMapping("/nao-autenticado")
        void naoAutenticado() {
            throw new NaoAutenticadoException("Usuário ou senha inválidos.");
        }

        @PostMapping(value = "/corpo", consumes = MediaType.APPLICATION_JSON_VALUE)
        void corpo(@Valid @RequestBody CorpoDeTeste corpo) {
        }

        @GetMapping("/pagina")
        void pagina(@RequestParam @Min(value = 1, message = "deve ser a partir de 1") int numero) {
        }

        @GetMapping("/numero/{numero}")
        void numero(@PathVariable int numero) {
        }

        @GetMapping("/concorrencia")
        void concorrencia() {
            throw new OptimisticLockingFailureException("registro alterado");
        }

        @GetMapping("/integridade")
        void integridade() {
            throw new DataIntegrityViolationException("duplicate key value violates unique constraint uk_placa");
        }

        @GetMapping("/inesperado")
        void inesperado() {
            throw new RuntimeException("senha do banco expirada");
        }
    }
}
