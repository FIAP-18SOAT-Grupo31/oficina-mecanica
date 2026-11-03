package br.com.fiap.oficina_mecanica.cliente.infrastructure.controller;

import br.com.fiap.oficina_mecanica.cliente.infrastructure.TesteIntegracaoCliente;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TesteIntegracaoCliente
class ClienteControllerIntegrationTest {

    private static final String CPF_MARIA = "529.982.247-25";
    private static final String CPF_JOAO = "000.000.037-00";
    private static final String CPF_ANA = "000.000.074-47";

    private static final String ENDERECO_JSON = """
            {"rua":"rua das flores","numero":"123","complemento":"Apto 12","bairro":"centro",\
            "cidade":"são paulo","estado":"SP","cep":"01001-000"}""";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbc;

    private static RequestPostProcessor jwtAtendente() {
        return jwt().authorities(new SimpleGrantedAuthority("ROLE_ATENDENTE"));
    }

    private static RequestPostProcessor jwtAdmin() {
        return jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    private static String corpo(String nome, String documento, String telefone, String email) {
        return """
                {"nome":"%s","documento":"%s","telefone":"%s","email":"%s","endereco":%s}"""
                .formatted(nome, documento, telefone, email, ENDERECO_JSON);
    }

    private static String corpoMaria() {
        return corpo("Maria Silva", CPF_MARIA, "(11) 98765-4321", "maria@email.com");
    }

    private String cadastrar(String corpo) throws Exception {
        String resposta = mvc.perform(post("/api/clientes").with(jwtAtendente())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(resposta, "$.id");
    }

    // ---------------------------------------------------------------- cadastro

    @Test
    void cadastraClienteERetorna201ComLocationEDadosNormalizados() throws Exception {
        mvc.perform(post("/api/clientes").with(jwtAtendente())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoMaria()))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/clientes/")))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.nome").value("Maria Silva"))
                .andExpect(jsonPath("$.documento").value("52998224725"))
                .andExpect(jsonPath("$.telefone").value("11987654321"))
                .andExpect(jsonPath("$.email").value("maria@email.com"))
                .andExpect(jsonPath("$.endereco.rua").value("Rua Das Flores"))
                .andExpect(jsonPath("$.endereco.numero").value("123"))
                .andExpect(jsonPath("$.endereco.complemento").value("Apto 12"))
                .andExpect(jsonPath("$.endereco.bairro").value("Centro"))
                .andExpect(jsonPath("$.endereco.cidade").value("São Paulo"))
                .andExpect(jsonPath("$.endereco.estado").value("SP"))
                .andExpect(jsonPath("$.endereco.cep").value("01001000"));
    }

    @Test
    void cadastraClienteComPerfilAdmin() throws Exception {
        mvc.perform(post("/api/clientes").with(jwtAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoMaria()))
                .andExpect(status().isCreated());
    }

    @Test
    void cadastroComDocumentoJaCadastradoRetorna422() throws Exception {
        cadastrar(corpoMaria());

        mvc.perform(post("/api/clientes").with(jwtAtendente())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo("Outra Maria", "52998224725", "(11) 98765-4321", "outra@email.com")))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.erro").value("Violação de Regra de Negócio"))
                .andExpect(jsonPath("$.mensagem").value("Já existe um cliente cadastrado com este documento."));
    }

    @Test
    void cadastroComCpfInvalidoRetorna422ComAMensagemDoDominio() throws Exception {
        mvc.perform(post("/api/clientes").with(jwtAtendente())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo("Maria Silva", "529.982.247-24", "(11) 98765-4321", "maria@email.com")))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.erro").value("Violação de Regra de Negócio"))
                .andExpect(jsonPath("$.mensagem").value("O documento do cliente é inválido"));
    }

    @Test
    void cadastroComEmailInvalidoRetorna422ComAMensagemDoDominio() throws Exception {
        mvc.perform(post("/api/clientes").with(jwtAtendente())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo("Maria Silva", CPF_MARIA, "(11) 98765-4321", "sem-arroba")))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.mensagem").value("O e-mail do cliente é inválido"));
    }

    @Test
    void cadastroSemCamposObrigatoriosRetorna400() throws Exception {
        mvc.perform(post("/api/clientes").with(jwtAtendente())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Dados Inválidos"))
                .andExpect(jsonPath("$.mensagem", containsString("nome: é obrigatório")))
                .andExpect(jsonPath("$.mensagem", containsString("documento: é obrigatório")))
                .andExpect(jsonPath("$.mensagem", containsString("telefone: é obrigatório")))
                .andExpect(jsonPath("$.mensagem", containsString("email: é obrigatório")))
                .andExpect(jsonPath("$.mensagem", containsString("endereco: é obrigatório")));
    }

    @Test
    void cadastroComEnderecoIncompletoRetorna400() throws Exception {
        String corpo = """
                {"nome":"Maria Silva","documento":"529.982.247-25","telefone":"(11) 98765-4321",\
                "email":"maria@email.com","endereco":{}}""";

        mvc.perform(post("/api/clientes").with(jwtAtendente())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Dados Inválidos"))
                .andExpect(jsonPath("$.mensagem", containsString("endereco.rua: é obrigatório")))
                .andExpect(jsonPath("$.mensagem", containsString("endereco.estado: é obrigatório")))
                .andExpect(jsonPath("$.mensagem", containsString("endereco.cep: é obrigatório")));
    }

    @Test
    void cadastroComNomeMaiorQueOLimiteDaColunaRetorna400() throws Exception {
        mvc.perform(post("/api/clientes").with(jwtAtendente())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo("a".repeat(151), CPF_MARIA, "(11) 98765-4321", "maria@email.com")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("nome: deve ter no máximo 150 caracteres"));
    }

    @Test
    void cadastroComEstadoInexistenteRetorna400() throws Exception {
        String corpo = corpoMaria().replace("\"estado\":\"SP\"", "\"estado\":\"XX\"");

        mvc.perform(post("/api/clientes").with(jwtAtendente())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Requisição Inválida"));
    }

    // ------------------------------------------------------------------ busca

    @Test
    void buscaClientePorId() throws Exception {
        String id = cadastrar(corpoMaria());

        mvc.perform(get("/api/clientes/" + id).with(jwtAtendente()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.nome").value("Maria Silva"))
                .andExpect(jsonPath("$.documento").value("52998224725"))
                .andExpect(jsonPath("$.endereco.cidade").value("São Paulo"));
    }

    @Test
    void buscaClienteInexistenteRetorna404() throws Exception {
        mvc.perform(get("/api/clientes/" + UUID.randomUUID()).with(jwtAtendente()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.erro").value("Recurso Não Encontrado"))
                .andExpect(jsonPath("$.mensagem").value("Cliente não encontrado."));
    }

    @Test
    void buscaComIdMalFormadoRetorna400() throws Exception {
        mvc.perform(get("/api/clientes/nao-e-um-uuid").with(jwtAtendente()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Requisição Inválida"));
    }

    // --------------------------------------------------------------- listagem

    private void cadastrarTresClientes() throws Exception {
        cadastrar(corpo("Maria Silva", CPF_MARIA, "(11) 98765-4321", "maria@email.com"));
        cadastrar(corpo("João Souza", CPF_JOAO, "(21) 3456-7890", "joao@email.com"));
        cadastrar(corpo("Maria Alves", CPF_ANA, "(31) 98765-4321", "alves@email.com"));
    }

    @Test
    void listaOsClientesPaginadosEOrdenadosPorNome() throws Exception {
        cadastrarTresClientes();

        mvc.perform(get("/api/clientes").with(jwtAtendente()).param("tamanho", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conteudo", hasSize(2)))
                .andExpect(jsonPath("$.conteudo[0].nome").value("João Souza"))
                .andExpect(jsonPath("$.conteudo[1].nome").value("Maria Alves"))
                .andExpect(jsonPath("$.pagina").value(0))
                .andExpect(jsonPath("$.tamanho").value(2))
                .andExpect(jsonPath("$.totalElementos").value(3))
                .andExpect(jsonPath("$.totalPaginas").value(2));

        mvc.perform(get("/api/clientes").with(jwtAtendente()).param("tamanho", "2").param("pagina", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conteudo", hasSize(1)))
                .andExpect(jsonPath("$.conteudo[0].nome").value("Maria Silva"))
                .andExpect(jsonPath("$.pagina").value(1));
    }

    @Test
    void listaComPaginacaoPadraoQuandoNaoInformada() throws Exception {
        cadastrarTresClientes();

        mvc.perform(get("/api/clientes").with(jwtAtendente()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conteudo", hasSize(3)))
                .andExpect(jsonPath("$.pagina").value(0))
                .andExpect(jsonPath("$.tamanho").value(20))
                .andExpect(jsonPath("$.totalPaginas").value(1));
    }

    @Test
    void filtraPorNomeEPorDocumentoComOuSemMascara() throws Exception {
        cadastrarTresClientes();

        mvc.perform(get("/api/clientes").with(jwtAtendente()).param("nome", "maria"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conteudo", hasSize(2)))
                .andExpect(jsonPath("$.totalElementos").value(2));

        mvc.perform(get("/api/clientes").with(jwtAtendente()).param("documento", "000.000.037-00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conteudo", hasSize(1)))
                .andExpect(jsonPath("$.conteudo[0].nome").value("João Souza"));

        mvc.perform(get("/api/clientes").with(jwtAtendente()).param("nome", "maria").param("documento", CPF_JOAO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conteudo", hasSize(0)))
                .andExpect(jsonPath("$.totalElementos").value(0))
                .andExpect(jsonPath("$.totalPaginas").value(0));
    }

    @Test
    void listagemComParametrosDePaginacaoForaDoLimiteRetorna400() throws Exception {
        mvc.perform(get("/api/clientes").with(jwtAtendente()).param("tamanho", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Dados Inválidos"))
                .andExpect(jsonPath("$.mensagem").value("tamanho: deve ser maior ou igual a 1"));

        mvc.perform(get("/api/clientes").with(jwtAtendente()).param("tamanho", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("tamanho: deve ser menor ou igual a 100"));

        mvc.perform(get("/api/clientes").with(jwtAtendente()).param("pagina", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("pagina: deve ser maior ou igual a 0"));
    }

    // ------------------------------------------------------------- atualização

    @Test
    void atualizaTodosOsDadosDoCliente() throws Exception {
        String id = cadastrar(corpoMaria());

        mvc.perform(put("/api/clientes/" + id).with(jwtAtendente())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo("João Souza", CPF_JOAO, "(21) 3456-7890", "Joao@Email.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.nome").value("João Souza"))
                .andExpect(jsonPath("$.documento").value("00000003700"))
                .andExpect(jsonPath("$.telefone").value("2134567890"))
                .andExpect(jsonPath("$.email").value("joao@email.com"));

        mvc.perform(get("/api/clientes/" + id).with(jwtAtendente()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("João Souza"))
                .andExpect(jsonPath("$.documento").value("00000003700"));
    }

    @Test
    void atualizaMantendoOProprioDocumento() throws Exception {
        String id = cadastrar(corpoMaria());

        mvc.perform(put("/api/clientes/" + id).with(jwtAtendente())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo("Maria Souza", "52998224725", "(11) 98765-4321", "maria@email.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Maria Souza"))
                .andExpect(jsonPath("$.documento").value("52998224725"));
    }

    @Test
    void atualizacaoComDocumentoDeOutroClienteRetorna422() throws Exception {
        String id = cadastrar(corpoMaria());
        cadastrar(corpo("João Souza", CPF_JOAO, "(21) 3456-7890", "joao@email.com"));

        mvc.perform(put("/api/clientes/" + id).with(jwtAtendente())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo("Maria Silva", CPF_JOAO, "(11) 98765-4321", "maria@email.com")))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.mensagem").value("Já existe outro cliente cadastrado com este documento."));
    }

    @Test
    void atualizacaoComTelefoneInvalidoRetorna422() throws Exception {
        String id = cadastrar(corpoMaria());

        mvc.perform(put("/api/clientes/" + id).with(jwtAtendente())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo("Maria Silva", CPF_MARIA, "123", "maria@email.com")))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.mensagem").value("O telefone do cliente é inválido"));
    }

    @Test
    void atualizacaoSemCamposObrigatoriosRetorna400() throws Exception {
        String id = cadastrar(corpoMaria());

        mvc.perform(put("/api/clientes/" + id).with(jwtAtendente())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Dados Inválidos"));
    }

    @Test
    void atualizacaoDeClienteInexistenteRetorna404() throws Exception {
        mvc.perform(put("/api/clientes/" + UUID.randomUUID()).with(jwtAtendente())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoMaria()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Cliente não encontrado."));
    }

    // ---------------------------------------------------------------- exclusão

    @Test
    void excluiClienteSemVeiculos() throws Exception {
        String id = cadastrar(corpoMaria());

        mvc.perform(delete("/api/clientes/" + id).with(jwtAtendente()))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/clientes/" + id).with(jwtAtendente()))
                .andExpect(status().isNotFound());
    }

    @Test
    void exclusaoDeClienteInexistenteRetorna404() throws Exception {
        mvc.perform(delete("/api/clientes/" + UUID.randomUUID()).with(jwtAtendente()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Cliente não encontrado."));
    }

    @Test
    void exclusaoDeClienteComVeiculosRetorna422EMantemOCliente() throws Exception {
        UUID clienteId = UUID.randomUUID();
        jdbc.update("INSERT INTO clientes (id, nome, documento, email, telefone, rua, numero, bairro, cidade, cep, estado) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                clienteId, "Cliente com veículo", "52998224725", "cliente@email.com", "11987654321",
                "Rua Das Flores", "123", "Centro", "São Paulo", "01001000", "SP");
        jdbc.update("INSERT INTO veiculos (id, cliente_id, placa, marca, modelo, ano) VALUES (?, ?, ?, ?, ?, ?)",
                UUID.randomUUID(), clienteId, "ABC1D23", "Fiat", "Argo", 2022);

        mvc.perform(delete("/api/clientes/" + clienteId).with(jwtAtendente()))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.erro").value("Violação de Regra de Negócio"))
                .andExpect(jsonPath("$.mensagem")
                        .value("Não é possível excluir o cliente: existem veículos vinculados a ele."));

        mvc.perform(get("/api/clientes/" + clienteId).with(jwtAtendente()))
                .andExpect(status().isOk());
    }

    // --------------------------------------------------------------- segurança

    @Test
    void todasAsRotasExigemToken() throws Exception {
        UUID id = UUID.randomUUID();

        mvc.perform(post("/api/clientes").contentType(MediaType.APPLICATION_JSON).content(corpoMaria()))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/clientes"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/clientes/" + id))
                .andExpect(status().isUnauthorized());
        mvc.perform(put("/api/clientes/" + id).contentType(MediaType.APPLICATION_JSON).content(corpoMaria()))
                .andExpect(status().isUnauthorized());
        mvc.perform(delete("/api/clientes/" + id))
                .andExpect(status().isUnauthorized());
    }
}
