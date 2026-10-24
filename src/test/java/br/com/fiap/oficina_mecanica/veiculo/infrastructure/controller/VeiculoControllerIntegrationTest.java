package br.com.fiap.oficina_mecanica.veiculo.infrastructure.controller;

import br.com.fiap.oficina_mecanica.cliente.infrastructure.TesteIntegracaoCliente;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
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
class VeiculoControllerIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbc;

    private UUID clienteA;
    private UUID clienteB;

    private static RequestPostProcessor jwtAtendente() {
        return jwt().authorities(new SimpleGrantedAuthority("ROLE_ATENDENTE"));
    }

    private UUID inserirCliente(String documento) {
        UUID id = UUID.randomUUID();
        jdbc.update("INSERT INTO clientes (id, nome, documento, email, telefone, rua, numero, bairro, cidade, cep, "
                        + "estado) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                id, "Cliente " + documento, documento, "cliente@email.com", "11987654321",
                "Rua Das Flores", "123", "Centro", "São Paulo", "01001000", "SP");
        return id;
    }

    @BeforeEach
    void cadastrarClientes() {
        clienteA = inserirCliente("52998224725");
        clienteB = inserirCliente("00000003700");
    }

    private static String corpo(UUID clienteId, String placa, String marca, String modelo, int ano) {
        return """
                {"clienteId":"%s","placa":"%s","marca":"%s","modelo":"%s","anoFabricacao":%d}"""
                .formatted(clienteId, placa, marca, modelo, ano);
    }

    private String cadastrar(String corpo) throws Exception {
        String resposta = mvc.perform(post("/api/veiculos").with(jwtAtendente())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(resposta, "$.id");
    }

    // ---------------------------------------------------------------- cadastro

    @Test
    void cadastraVeiculoERetorna201ComLocationEDadosNormalizados() throws Exception {
        mvc.perform(post("/api/veiculos").with(jwtAtendente())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo(clienteA, "abc-1d23", " Fiat ", " Argo ", 2022)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/veiculos/")))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.clienteId").value(clienteA.toString()))
                .andExpect(jsonPath("$.placa").value("ABC1D23"))
                .andExpect(jsonPath("$.marca").value("Fiat"))
                .andExpect(jsonPath("$.modelo").value("Argo"))
                .andExpect(jsonPath("$.anoFabricacao").value(2022));
    }

    @Test
    void cadastroParaClienteInexistenteRetorna404() throws Exception {
        mvc.perform(post("/api/veiculos").with(jwtAtendente())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo(UUID.randomUUID(), "ABC1D23", "Fiat", "Argo", 2022)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Cliente não encontrado."));
    }

    @Test
    void cadastroComPlacaJaCadastradaRetorna422() throws Exception {
        cadastrar(corpo(clienteA, "ABC1D23", "Fiat", "Argo", 2022));

        mvc.perform(post("/api/veiculos").with(jwtAtendente())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo(clienteB, "abc-1d23", "Fiat", "Argo", 2022)))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.erro").value("Violação de Regra de Negócio"))
                .andExpect(jsonPath("$.mensagem").value("Já existe um veículo cadastrado com esta placa."));
    }

    @Test
    void cadastroComPlacaInvalidaRetorna422ComAMensagemDoDominio() throws Exception {
        mvc.perform(post("/api/veiculos").with(jwtAtendente())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo(clienteA, "AB123", "Fiat", "Argo", 2022)))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.mensagem").value("A placa do veículo é inválida."));
    }

    @Test
    void cadastroComAnoFuturoRetorna422() throws Exception {
        mvc.perform(post("/api/veiculos").with(jwtAtendente())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo(clienteA, "ABC1D23", "Fiat", "Argo", 2999)))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.mensagem").value("Ano de fabricação do veículo é inválido."));
    }

    @Test
    void cadastroSemCamposObrigatoriosRetorna400() throws Exception {
        mvc.perform(post("/api/veiculos").with(jwtAtendente())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Dados Inválidos"))
                .andExpect(jsonPath("$.mensagem", containsString("clienteId: é obrigatório")))
                .andExpect(jsonPath("$.mensagem", containsString("placa: é obrigatório")))
                .andExpect(jsonPath("$.mensagem", containsString("marca: é obrigatório")))
                .andExpect(jsonPath("$.mensagem", containsString("modelo: é obrigatório")))
                .andExpect(jsonPath("$.mensagem", containsString("anoFabricacao: é obrigatório")));
    }

    @Test
    void cadastroComMarcaMaiorQueOLimiteDaColunaRetorna400() throws Exception {
        mvc.perform(post("/api/veiculos").with(jwtAtendente())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo(clienteA, "ABC1D23", "a".repeat(51), "Argo", 2022)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("marca: deve ter no máximo 50 caracteres"));
    }

    @Test
    void cadastroComClienteIdMalFormadoRetorna400() throws Exception {
        mvc.perform(post("/api/veiculos").with(jwtAtendente())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo(clienteA, "ABC1D23", "Fiat", "Argo", 2022)
                                .replace(clienteA.toString(), "nao-e-um-uuid")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Requisição Inválida"));
    }

    // ------------------------------------------------------------------ busca

    @Test
    void buscaVeiculoPorId() throws Exception {
        String id = cadastrar(corpo(clienteA, "ABC1D23", "Fiat", "Argo", 2022));

        mvc.perform(get("/api/veiculos/" + id).with(jwtAtendente()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.placa").value("ABC1D23"));
    }

    @Test
    void buscaVeiculoInexistenteRetorna404() throws Exception {
        mvc.perform(get("/api/veiculos/" + UUID.randomUUID()).with(jwtAtendente()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Veículo não encontrado."));
    }

    @Test
    void buscaComIdMalFormadoRetorna400() throws Exception {
        mvc.perform(get("/api/veiculos/nao-e-um-uuid").with(jwtAtendente()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Requisição Inválida"));
    }

    // --------------------------------------------------------------- listagem

    private void cadastrarTresVeiculos() throws Exception {
        cadastrar(corpo(clienteA, "ABC1D23", "Fiat", "Argo", 2022));
        cadastrar(corpo(clienteA, "ABC1234", "Honda", "Civic", 2018));
        cadastrar(corpo(clienteB, "QWE1R23", "VW", "Gol", 2015));
    }

    @Test
    void listaPaginadoEOrdenadoPorPlaca() throws Exception {
        cadastrarTresVeiculos();

        mvc.perform(get("/api/veiculos").with(jwtAtendente()).param("tamanho", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conteudo", hasSize(2)))
                .andExpect(jsonPath("$.conteudo[0].placa").value("ABC1234"))
                .andExpect(jsonPath("$.conteudo[1].placa").value("ABC1D23"))
                .andExpect(jsonPath("$.totalElementos").value(3))
                .andExpect(jsonPath("$.totalPaginas").value(2));

        mvc.perform(get("/api/veiculos").with(jwtAtendente()).param("tamanho", "2").param("pagina", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conteudo", hasSize(1)))
                .andExpect(jsonPath("$.conteudo[0].placa").value("QWE1R23"));
    }

    @Test
    void listaComPaginacaoPadraoQuandoNaoInformada() throws Exception {
        cadastrarTresVeiculos();

        mvc.perform(get("/api/veiculos").with(jwtAtendente()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conteudo", hasSize(3)))
                .andExpect(jsonPath("$.pagina").value(0))
                .andExpect(jsonPath("$.tamanho").value(20));
    }

    @Test
    void filtraPorClientePlacaEModelo() throws Exception {
        cadastrarTresVeiculos();

        mvc.perform(get("/api/veiculos").with(jwtAtendente()).param("clienteId", clienteA.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conteudo", hasSize(2)))
                .andExpect(jsonPath("$.totalElementos").value(2));

        mvc.perform(get("/api/veiculos").with(jwtAtendente()).param("placa", "abc-1d23"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conteudo", hasSize(1)))
                .andExpect(jsonPath("$.conteudo[0].modelo").value("Argo"));

        mvc.perform(get("/api/veiculos").with(jwtAtendente()).param("modelo", "CIV"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conteudo", hasSize(1)))
                .andExpect(jsonPath("$.conteudo[0].placa").value("ABC1234"));

        mvc.perform(get("/api/veiculos").with(jwtAtendente())
                        .param("clienteId", clienteB.toString()).param("modelo", "argo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conteudo", hasSize(0)))
                .andExpect(jsonPath("$.totalPaginas").value(0));
    }

    @Test
    void listagemComParametrosInvalidosRetorna400() throws Exception {
        mvc.perform(get("/api/veiculos").with(jwtAtendente()).param("tamanho", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("tamanho: deve ser maior ou igual a 1"));

        mvc.perform(get("/api/veiculos").with(jwtAtendente()).param("tamanho", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("tamanho: deve ser menor ou igual a 100"));

        mvc.perform(get("/api/veiculos").with(jwtAtendente()).param("pagina", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("pagina: deve ser maior ou igual a 0"));

        mvc.perform(get("/api/veiculos").with(jwtAtendente()).param("clienteId", "nao-e-um-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Requisição Inválida"));
    }

    // ------------------------------------------------------------- atualização

    @Test
    void atualizaTodosOsDadosInclusiveTransferindoOCliente() throws Exception {
        String id = cadastrar(corpo(clienteA, "ABC1D23", "Fiat", "Argo", 2022));

        mvc.perform(put("/api/veiculos/" + id).with(jwtAtendente())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo(clienteB, "xyz-9k88", "Honda", "Civic", 2020)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.clienteId").value(clienteB.toString()))
                .andExpect(jsonPath("$.placa").value("XYZ9K88"))
                .andExpect(jsonPath("$.marca").value("Honda"))
                .andExpect(jsonPath("$.modelo").value("Civic"))
                .andExpect(jsonPath("$.anoFabricacao").value(2020));

        mvc.perform(get("/api/veiculos/" + id).with(jwtAtendente()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clienteId").value(clienteB.toString()));
    }

    @Test
    void atualizaMantendoAPropriaPlaca() throws Exception {
        String id = cadastrar(corpo(clienteA, "ABC1D23", "Fiat", "Argo", 2022));

        mvc.perform(put("/api/veiculos/" + id).with(jwtAtendente())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo(clienteA, "ABC1D23", "Fiat", "Argo", 2021)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.anoFabricacao").value(2021));
    }

    @Test
    void atualizacaoComPlacaDeOutroVeiculoRetorna422() throws Exception {
        String id = cadastrar(corpo(clienteA, "ABC1D23", "Fiat", "Argo", 2022));
        cadastrar(corpo(clienteB, "XYZ9K88", "Honda", "Civic", 2020));

        mvc.perform(put("/api/veiculos/" + id).with(jwtAtendente())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo(clienteA, "XYZ9K88", "Fiat", "Argo", 2022)))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.mensagem").value("Já existe outro veículo cadastrado com esta placa."));
    }

    @Test
    void atualizacaoComPlacaInvalidaRetorna422() throws Exception {
        String id = cadastrar(corpo(clienteA, "ABC1D23", "Fiat", "Argo", 2022));

        mvc.perform(put("/api/veiculos/" + id).with(jwtAtendente())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo(clienteA, "AB123", "Fiat", "Argo", 2022)))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.mensagem").value("A placa do veículo é inválida."));
    }

    @Test
    void atualizacaoParaClienteInexistenteRetorna404() throws Exception {
        String id = cadastrar(corpo(clienteA, "ABC1D23", "Fiat", "Argo", 2022));

        mvc.perform(put("/api/veiculos/" + id).with(jwtAtendente())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo(UUID.randomUUID(), "ABC1D23", "Fiat", "Argo", 2022)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Cliente não encontrado."));
    }

    @Test
    void atualizacaoSemCamposObrigatoriosRetorna400() throws Exception {
        String id = cadastrar(corpo(clienteA, "ABC1D23", "Fiat", "Argo", 2022));

        mvc.perform(put("/api/veiculos/" + id).with(jwtAtendente())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Dados Inválidos"));
    }

    @Test
    void atualizacaoDeVeiculoInexistenteRetorna404() throws Exception {
        mvc.perform(put("/api/veiculos/" + UUID.randomUUID()).with(jwtAtendente())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo(clienteA, "ABC1D23", "Fiat", "Argo", 2022)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Veículo não encontrado."));
    }

    // ---------------------------------------------------------------- exclusão

    @Test
    void excluiOVeiculo() throws Exception {
        String id = cadastrar(corpo(clienteA, "ABC1D23", "Fiat", "Argo", 2022));

        mvc.perform(delete("/api/veiculos/" + id).with(jwtAtendente()))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/veiculos/" + id).with(jwtAtendente()))
                .andExpect(status().isNotFound());
    }

    @Test
    void exclusaoDeVeiculoInexistenteRetorna404() throws Exception {
        mvc.perform(delete("/api/veiculos/" + UUID.randomUUID()).with(jwtAtendente()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Veículo não encontrado."));
    }

    @Test
    void clienteComVeiculoNaoPodeSerExcluidoAteOVeiculoSerRemovido() throws Exception {
        String veiculoId = cadastrar(corpo(clienteA, "ABC1D23", "Fiat", "Argo", 2022));

        mvc.perform(delete("/api/clientes/" + clienteA).with(jwtAtendente()))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.mensagem")
                        .value("Não é possível excluir o cliente: existem veículos vinculados a ele."));

        mvc.perform(delete("/api/veiculos/" + veiculoId).with(jwtAtendente()))
                .andExpect(status().isNoContent());

        mvc.perform(delete("/api/clientes/" + clienteA).with(jwtAtendente()))
                .andExpect(status().isNoContent());
    }

    // --------------------------------------------------------------- segurança

    @Test
    void todasAsRotasExigemToken() throws Exception {
        UUID id = UUID.randomUUID();
        String corpo = corpo(clienteA, "ABC1D23", "Fiat", "Argo", 2022);

        mvc.perform(post("/api/veiculos").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/veiculos"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/veiculos/" + id))
                .andExpect(status().isUnauthorized());
        mvc.perform(put("/api/veiculos/" + id).contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isUnauthorized());
        mvc.perform(delete("/api/veiculos/" + id))
                .andExpect(status().isUnauthorized());
    }
}
