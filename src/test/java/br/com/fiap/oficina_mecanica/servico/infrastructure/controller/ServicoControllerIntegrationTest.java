package br.com.fiap.oficina_mecanica.servico.infrastructure.controller;

import com.jayway.jsonpath.JsonPath;
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
import org.springframework.transaction.annotation.Transactional;

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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"test", "servicos-test"})
@Transactional
class ServicoControllerIntegrationTest {

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

    private static String corpo(String nome, String descricao, String valor) {
        String descricaoJson = descricao == null ? "null" : "\"" + descricao + "\"";
        return """
                {"nome":"%s","descricao":%s,"valorMaoDeObra":%s}""".formatted(nome, descricaoJson, valor);
    }

    private String cadastrar(String nome, String valor) throws Exception {
        String resposta = mvc.perform(post("/api/servicos").with(jwtAtendente())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo(nome, null, valor)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(resposta, "$.id");
    }

    // ---------------------------------------------------------------- cadastro

    @Test
    void cadastraServicoERetorna201ComLocation() throws Exception {
        mvc.perform(post("/api/servicos").with(jwtAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo("  Troca de óleo ", "Óleo e filtro", "150.00")))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/servicos/")))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.nome").value("Troca de óleo"))
                .andExpect(jsonPath("$.descricao").value("Óleo e filtro"))
                .andExpect(jsonPath("$.valorMaoDeObra").value(150.00));
    }

    @Test
    void cadastroComDadosInvalidosRetorna400ComOsCampos() throws Exception {
        mvc.perform(post("/api/servicos").with(jwtAtendente())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\" \",\"valorMaoDeObra\":-1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem", containsString("nome")))
                .andExpect(jsonPath("$.mensagem", containsString("valorMaoDeObra")));
    }

    @Test
    void cadastroComValorComMaisDeDuasCasasRetorna400() throws Exception {
        mvc.perform(post("/api/servicos").with(jwtAtendente())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo("Alinhamento", null, "10.999")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cadastroComNomeRepetidoRetorna422() throws Exception {
        cadastrar("Alinhamento", "80.00");

        mvc.perform(post("/api/servicos").with(jwtAtendente())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo("ALINHAMENTO", null, "90.00")))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.mensagem").value("Já existe um serviço cadastrado com este nome."));
    }

    // ---------------------------------------------------------------- consulta

    @Test
    void buscaServicoPeloId() throws Exception {
        String id = cadastrar("Alinhamento", "80.00");

        mvc.perform(get("/api/servicos/" + id).with(jwtAtendente()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.nome").value("Alinhamento"));
    }

    @Test
    void buscarServicoInexistenteRetorna404() throws Exception {
        mvc.perform(get("/api/servicos/" + UUID.randomUUID()).with(jwtAtendente()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Serviço não encontrado."));
    }

    @Test
    void listaServicosPaginadosEFiltradosPorNome() throws Exception {
        cadastrar("Troca de óleo", "150.00");
        cadastrar("Alinhamento", "80.00");
        cadastrar("Balanceamento", "60.00");

        mvc.perform(get("/api/servicos").param("tamanho", "2").with(jwtAtendente()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conteudo", hasSize(2)))
                .andExpect(jsonPath("$.conteudo[0].nome").value("Alinhamento"))
                .andExpect(jsonPath("$.totalElementos").value(3))
                .andExpect(jsonPath("$.totalPaginas").value(2));

        mvc.perform(get("/api/servicos").param("nome", "ÓLEO").with(jwtAtendente()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conteudo", hasSize(1)))
                .andExpect(jsonPath("$.conteudo[0].nome").value("Troca de óleo"));
    }

    @Test
    void listagemComTamanhoInvalidoRetorna400() throws Exception {
        mvc.perform(get("/api/servicos").param("tamanho", "101").with(jwtAtendente()))
                .andExpect(status().isBadRequest());
    }

    // ---------------------------------------------------------------- atualização

    @Test
    void atualizaServico() throws Exception {
        String id = cadastrar("Alinhamento", "80.00");

        mvc.perform(put("/api/servicos/" + id).with(jwtAtendente())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo("Alinhamento 3D", "Com laudo", "120.50")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Alinhamento 3D"))
                .andExpect(jsonPath("$.descricao").value("Com laudo"))
                .andExpect(jsonPath("$.valorMaoDeObra").value(120.50));
    }

    @Test
    void atualizarParaNomeDeOutroServicoRetorna422() throws Exception {
        cadastrar("Troca de óleo", "150.00");
        String id = cadastrar("Alinhamento", "80.00");

        mvc.perform(put("/api/servicos/" + id).with(jwtAtendente())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo("troca de óleo", null, "80.00")))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.mensagem").value("Já existe outro serviço cadastrado com este nome."));
    }

    @Test
    void atualizarServicoInexistenteRetorna404() throws Exception {
        mvc.perform(put("/api/servicos/" + UUID.randomUUID()).with(jwtAtendente())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo("Alinhamento", null, "80.00")))
                .andExpect(status().isNotFound());
    }

    // ---------------------------------------------------------------- exclusão

    @Test
    void excluiServicoNaoUsado() throws Exception {
        String id = cadastrar("Alinhamento", "80.00");

        mvc.perform(delete("/api/servicos/" + id).with(jwtAtendente()))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/servicos/" + id).with(jwtAtendente()))
                .andExpect(status().isNotFound());
    }

    @Test
    void naoExcluiServicoUsadoEmOrcamento() throws Exception {
        String id = cadastrar("Alinhamento", "80.00");
        inserirOrcamentoUsando(UUID.fromString(id));

        mvc.perform(delete("/api/servicos/" + id).with(jwtAtendente()))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.mensagem")
                        .value("Não é possível excluir o serviço: ele já foi usado em orçamentos."));

        mvc.perform(get("/api/servicos/" + id).with(jwtAtendente()))
                .andExpect(status().isOk());
    }

    @Test
    void excluirServicoInexistenteRetorna404() throws Exception {
        mvc.perform(delete("/api/servicos/" + UUID.randomUUID()).with(jwtAtendente()))
                .andExpect(status().isNotFound());
    }

    // ---------------------------------------------------------------- segurança

    @Test
    void semTokenRetorna401() throws Exception {
        mvc.perform(get("/api/servicos"))
                .andExpect(status().isUnauthorized());
    }

    private void inserirOrcamentoUsando(UUID servicoId) {
        UUID clienteId = UUID.randomUUID();
        UUID veiculoId = UUID.randomUUID();
        UUID ordemServicoId = UUID.randomUUID();
        UUID orcamentoId = UUID.randomUUID();
        jdbc.update("INSERT INTO clientes (id, nome, documento, rua, numero, bairro, cidade, cep, estado) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                clienteId, "Cliente de teste", "52998224725", "Rua das Flores", "123", "Centro", "São Paulo",
                "01001000", "SP");
        jdbc.update("INSERT INTO veiculos (id, cliente_id, placa, marca, modelo, ano) VALUES (?, ?, ?, ?, ?, ?)",
                veiculoId, clienteId, "ABC1D23", "Fiat", "Argo", 2022);
        jdbc.update("INSERT INTO ordens_servico (id, cliente_id, veiculo_id, relato_problema, status, data_abertura, "
                        + "codigo_acesso) VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?)",
                ordemServicoId, clienteId, veiculoId, "Barulho na suspensão dianteira", "AGUARDANDO_APROVACAO",
                "TESTE1");
        jdbc.update("INSERT INTO orcamentos (id, ordem_servico_id, valor_total, status, data_criacao, data_validade) "
                        + "VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                orcamentoId, ordemServicoId, 80, "PENDENTE");
        jdbc.update("INSERT INTO orcamento_itens_servico (id, orcamento_id, catalogo_servico_id, descricao, "
                        + "valor_mao_de_obra) VALUES (?, ?, ?, ?, ?)",
                UUID.randomUUID(), orcamentoId, servicoId, "Alinhamento", 80);
    }
}
