package br.com.fiap.oficina_mecanica.ordemservico;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
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
        String resposta = mvc.perform(post("/api/ordens-servico")
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

        String orcamento = mvc.perform(post("/api/orcamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ordemServicoId\":\"" + ordemServicoId + "\",\"dataValidade\":\"2099-12-31T23:59:59\",\"servicos\":[{\"catalogoServicoId\":\""
                                + UUID.randomUUID() + "\",\"descricao\":\"Troca de óleo\",\"valorMaoDeObra\":200.00}],"
                                + "\"pecas\":[]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDENTE"))
                .andReturn().getResponse().getContentAsString();
        String orcamentoId = JsonPath.read(orcamento, "$.id");

        mvc.perform(get("/api/orcamentos/" + orcamentoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ordemServicoId").value(ordemServicoId));
        mvc.perform(patch("/api/orcamentos/" + orcamentoId + "/aprovar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APROVADO"));
        mvc.perform(patch("/api/orcamentos/" + orcamentoId + "/aprovar"))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.erro").value("Violação de Regra de Negócio"));
    }

    @Test
    void clienteInexistenteVolta404() throws Exception {
        mvc.perform(post("/api/ordens-servico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoOrdemServico(UUID.randomUUID(), veiculoId, "Barulho na suspensão dianteira")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Cliente não encontrado."));
    }

    @Test
    void relatoCurtoVolta400ComOCampo() throws Exception {
        mvc.perform(post("/api/ordens-servico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoOrdemServico(clienteId, veiculoId, "curto")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("relatoProblema: deve ter entre 10 e 500 caracteres"));
    }

    @Test
    void orcamentoInexistenteVolta404() throws Exception {
        mvc.perform(get("/api/orcamentos/" + UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.erro").value("Recurso Não Encontrado"));
    }
}
