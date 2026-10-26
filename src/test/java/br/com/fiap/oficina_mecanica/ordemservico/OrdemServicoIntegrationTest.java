package br.com.fiap.oficina_mecanica.ordemservico;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrdemServicoIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbc;

    private UUID clienteId;
    private UUID veiculoId;

    private RequestPostProcessor jwtAtendente() {
        return jwt().authorities(new SimpleGrantedAuthority("ROLE_ATENDENTE"));
    }

    private RequestPostProcessor jwtAdmin() {
        return jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    @BeforeEach
    void cadastrarClienteEVeiculo() {
        clienteId = UUID.randomUUID();
        veiculoId = UUID.randomUUID();
        String documento = String.valueOf(Math.abs(clienteId.getMostSignificantBits())).substring(0, 11);
        String placa = clienteId.toString().substring(0, 7).toUpperCase();
        jdbc.update("INSERT INTO clientes (id, nome, documento) VALUES (?, ?, ?)", clienteId, "Cliente de teste", documento);
        jdbc.update("INSERT INTO veiculos (id, cliente_id, placa, marca, modelo, ano) VALUES (?, ?, ?, ?, ?, ?)",
                veiculoId, clienteId, placa, "Fiat", "Argo", 2022);
    }

    private String corpoOrdemServico(UUID cliente, UUID veiculo, String relato) {
        return "{\"clienteId\":\"" + cliente + "\",\"veiculoId\":\"" + veiculo + "\",\"relatoProblema\":\"" + relato + "\"}";
    }

    private String criarOrdemServico() throws Exception {
        String resposta = mvc.perform(post("/api/ordens-servico").with(jwtAtendente())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoOrdemServico(clienteId, veiculoId, "Barulho na suspensão dianteira")))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/ordens-servico/")))
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(resposta, "$.ordemServicoId");
    }

    @Test
    void criaOrdemDeServicoEOrcamentoEAprovaOrcamento() throws Exception {
        String ordemServicoId = criarOrdemServico();

        String orcamento = mvc.perform(post("/api/orcamentos").with(jwtAtendente())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ordemServicoId\":\"" + ordemServicoId + "\",\"dataValidade\":\"2099-12-31T23:59:59\",\"servicos\":[{\"catalogoServicoId\":\""
                                + UUID.randomUUID() + "\",\"descricao\":\"Troca de óleo\",\"valorMaoDeObra\":200.00}],"
                                + "\"pecas\":[]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDENTE"))
                .andReturn().getResponse().getContentAsString();
        String orcamentoId = JsonPath.read(orcamento, "$.id");

        mvc.perform(get("/api/orcamentos/" + orcamentoId).with(jwtAtendente()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ordemServicoId").value(ordemServicoId));

        mvc.perform(patch("/api/orcamentos/" + orcamentoId + "/aprovar").with(jwtAtendente()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APROVADO"));

        mvc.perform(patch("/api/orcamentos/" + orcamentoId + "/aprovar").with(jwtAtendente()))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.erro").value("Violação de Regra de Negócio"));
    }

    @Test
    void clienteInexistenteVolta404() throws Exception {
        mvc.perform(post("/api/ordens-servico").with(jwtAtendente())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoOrdemServico(UUID.randomUUID(), veiculoId, "Barulho na suspensão dianteira")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Cliente não encontrado."));
    }

    @Test
    void relatoCurtoVolta400ComOCampo() throws Exception {
        mvc.perform(post("/api/ordens-servico").with(jwtAtendente())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoOrdemServico(clienteId, veiculoId, "curto")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("relatoProblema: deve ter entre 10 e 500 caracteres"));
    }

    @Test
    void orcamentoInexistenteVolta404() throws Exception {
        mvc.perform(get("/api/orcamentos/" + UUID.randomUUID()).with(jwtAtendente()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.erro").value("Recurso Não Encontrado"));
    }

    @Test
    void atendenteListaOrdensDeServicoComClienteEVeiculo() throws Exception {
        String ordemServicoId = criarOrdemServico();
        String ordem = "$.conteudo[?(@.id=='" + ordemServicoId + "')]";

        mvc.perform(get("/api/ordens-servico").with(jwtAtendente()))
                .andExpect(status().isOk())
                .andExpect(jsonPath(ordem + ".status").value("RECEBIDA"))
                .andExpect(jsonPath(ordem + ".cliente.id").value(clienteId.toString()))
                .andExpect(jsonPath(ordem + ".cliente.nome").value("Cliente de teste"))
                .andExpect(jsonPath(ordem + ".cliente.documento").isEmpty())
                .andExpect(jsonPath(ordem + ".veiculo.id").value(veiculoId.toString()))
                .andExpect(jsonPath(ordem + ".veiculo.marca").value("Fiat"))
                .andExpect(jsonPath(ordem + ".veiculo.modelo").value("Argo"))
                .andExpect(jsonPath(ordem + ".codigoAcesso").isEmpty())
                .andExpect(jsonPath("$.pagina").value(0))
                .andExpect(jsonPath("$.tamanho").value(20));
    }

    @Test
    void adminListaOrdensDeServico() throws Exception {
        String ordemServicoId = criarOrdemServico();

        mvc.perform(get("/api/ordens-servico").with(jwtAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conteudo[?(@.id=='" + ordemServicoId + "')]").isNotEmpty());
    }

    @Test
    void listaFiltrandoPorStatus() throws Exception {
        String ordemServicoId = criarOrdemServico();

        mvc.perform(get("/api/ordens-servico").param("status", "RECEBIDA").param("tamanho", "100").with(jwtAtendente()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conteudo[?(@.id=='" + ordemServicoId + "')]").isNotEmpty())
                .andExpect(jsonPath("$.conteudo[?(@.status!='RECEBIDA')]").isEmpty());

        mvc.perform(get("/api/ordens-servico").param("status", "FINALIZADA").param("tamanho", "100").with(jwtAtendente()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conteudo[?(@.id=='" + ordemServicoId + "')]").isEmpty());
    }

    @Test
    void listaPaginadaDevolveMaisRecentesPrimeiroEMetadados() throws Exception {
        criarOrdemServico();
        String maisRecente = criarOrdemServico();

        mvc.perform(get("/api/ordens-servico").param("pagina", "0").param("tamanho", "1").with(jwtAtendente()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conteudo.length()").value(1))
                .andExpect(jsonPath("$.conteudo[0].id").value(maisRecente))
                .andExpect(jsonPath("$.tamanho").value(1))
                .andExpect(jsonPath("$.totalElementos").value(org.hamcrest.Matchers.greaterThanOrEqualTo(2)))
                .andExpect(jsonPath("$.totalPaginas").value(org.hamcrest.Matchers.greaterThanOrEqualTo(2)));
    }

    @Test
    void paginaAlemDoFimVoltaVaziaSemErro() throws Exception {
        mvc.perform(get("/api/ordens-servico").param("pagina", "100000").with(jwtAtendente()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conteudo").isEmpty());
    }

    @Test
    void paginacaoInvalidaVolta400() throws Exception {
        mvc.perform(get("/api/ordens-servico").param("pagina", "-1").with(jwtAtendente()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Dados Inválidos"))
                .andExpect(jsonPath("$.mensagem").value("pagina: deve ser maior ou igual a 0"));
        mvc.perform(get("/api/ordens-servico").param("tamanho", "0").with(jwtAtendente()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("tamanho: deve ser entre 1 e 100"));
        mvc.perform(get("/api/ordens-servico").param("tamanho", "101").with(jwtAtendente()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("tamanho: deve ser entre 1 e 100"));
    }

    @Test
    void listaComStatusInvalidoVolta400() throws Exception {
        mvc.perform(get("/api/ordens-servico").param("status", "INEXISTENTE").with(jwtAtendente()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Requisição Inválida"));
    }

    @Test
    void listaSemTokenVolta401() throws Exception {
        mvc.perform(get("/api/ordens-servico"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.erro").value("Não Autenticado"));
    }

    @Test
    void listaComPapelSemPermissaoVolta403() throws Exception {
        mvc.perform(get("/api/ordens-servico")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CLIENTE"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.erro").value("Acesso Negado"));
    }
}