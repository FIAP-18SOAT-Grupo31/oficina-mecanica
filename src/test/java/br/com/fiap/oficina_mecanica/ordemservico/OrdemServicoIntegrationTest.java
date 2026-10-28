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
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;
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
    private String documento;
    private String placa;

    private RequestPostProcessor jwtAtendente() {
        return jwt().authorities(new SimpleGrantedAuthority("ROLE_ATENDENTE"));
    }

    @BeforeEach
    void cadastrarClienteEVeiculo() {
        clienteId = UUID.randomUUID();
        veiculoId = UUID.randomUUID();
        documento = cpfAleatorio();
        placa = placaAleatoria();
        jdbc.update("INSERT INTO clientes (id, nome, documento, email, telefone, rua, numero, bairro, cidade, cep, estado) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                clienteId, "Cliente de teste", documento, "cliente@email.com", "11987654321", "Rua das Flores", "123", "Centro", "São Paulo", "01001000", "SP");
        jdbc.update("INSERT INTO veiculos (id, cliente_id, placa, marca, modelo, ano) VALUES (?, ?, ?, ?, ?, ?)",
                veiculoId, clienteId, placa, "Fiat", "Argo", 2022);
    }

    // Cliente e veículo são carregados pelo domínio, que valida CPF e placa; por isso os dados de teste são válidos.
    private static String cpfAleatorio() {
        int[] digitos = new int[11];
        for (int i = 0; i < 9; i++) {
            digitos[i] = ThreadLocalRandom.current().nextInt(10);
        }
        digitos[9] = digitoVerificadorCpf(digitos, 9);
        digitos[10] = digitoVerificadorCpf(digitos, 10);
        StringBuilder cpf = new StringBuilder();
        for (int digito : digitos) {
            cpf.append(digito);
        }
        return cpf.toString();
    }

    private static int digitoVerificadorCpf(int[] digitos, int quantidade) {
        int soma = 0;
        for (int i = 0; i < quantidade; i++) {
            soma += digitos[i] * (quantidade + 1 - i);
        }
        int resto = 11 - (soma % 11);
        return resto > 9 ? 0 : resto;
    }

    private static String placaAleatoria() {
        ThreadLocalRandom aleatorio = ThreadLocalRandom.current();
        return "" + (char) ('A' + aleatorio.nextInt(26)) + (char) ('A' + aleatorio.nextInt(26))
                + (char) ('A' + aleatorio.nextInt(26)) + aleatorio.nextInt(10) + (char) ('A' + aleatorio.nextInt(26))
                + aleatorio.nextInt(10) + aleatorio.nextInt(10);
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

    private String corpoPorDocumentoEPlaca(String documentoCliente, String placaVeiculo) {
        return "{\"documentoCliente\":\"" + documentoCliente + "\",\"placaVeiculo\":\"" + placaVeiculo
                + "\",\"relatoProblema\":\"Barulho na suspensão dianteira\"}";
    }

    @Test
    void criaOrdemDeServicoPeloDocumentoDoClienteEPelaPlaca() throws Exception {
        // pontuação e minúsculas são ignoradas, como no cadastro
        String documentoFormatado = documento.substring(0, 3) + "." + documento.substring(3, 6) + "."
                + documento.substring(6, 9) + "-" + documento.substring(9);
        String placaFormatada = placa.substring(0, 3).toLowerCase() + "-" + placa.substring(3);

        String resposta = mvc.perform(post("/api/ordens-servico").with(jwtAtendente())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoPorDocumentoEPlaca(documentoFormatado, placaFormatada)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        UUID ordemServicoId = UUID.fromString(JsonPath.read(resposta, "$.ordemServicoId"));
        assertThat(jdbc.queryForObject("SELECT cliente_id FROM ordens_servico WHERE id = ?", UUID.class, ordemServicoId))
                .isEqualTo(clienteId);
        assertThat(jdbc.queryForObject("SELECT veiculo_id FROM ordens_servico WHERE id = ?", UUID.class, ordemServicoId))
                .isEqualTo(veiculoId);
    }

    @Test
    void documentoSemClienteVolta404() throws Exception {
        mvc.perform(post("/api/ordens-servico").with(jwtAtendente())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoPorDocumentoEPlaca("00000000000", placa)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Cliente não encontrado para o CPF/CNPJ informado."));
    }

    @Test
    void placaSemVeiculoVolta404() throws Exception {
        mvc.perform(post("/api/ordens-servico").with(jwtAtendente())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoPorDocumentoEPlaca(documento, "ZZZ9Z99")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Veículo não encontrado para a placa informada."));
    }

    @Test
    void veiculoDeOutroClienteVolta422() throws Exception {
        UUID outroCliente = UUID.randomUUID();
        jdbc.update("INSERT INTO clientes (id, nome, documento, email, telefone, rua, numero, bairro, cidade, cep, estado) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                outroCliente, "Outro cliente", cpfAleatorio(), "outro@email.com", "11987654322",
                "Rua das Flores", "123", "Centro", "São Paulo", "01001000", "SP");

        mvc.perform(post("/api/ordens-servico").with(jwtAtendente())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoOrdemServico(outroCliente, veiculoId, "Barulho na suspensão dianteira")))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.mensagem").value("O veículo informado não pertence ao cliente."));
    }

    @Test
    void clienteInformadoPorIdEDocumentoAoMesmoTempoVolta400() throws Exception {
        mvc.perform(post("/api/ordens-servico").with(jwtAtendente())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"clienteId\":\"" + clienteId + "\",\"documentoCliente\":\"" + documento
                                + "\",\"veiculoId\":\"" + veiculoId
                                + "\",\"relatoProblema\":\"Barulho na suspensão dianteira\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem", containsString("informe clienteId ou documentoCliente")));
    }

    @Test
    void semIdentificarOVeiculoVolta400() throws Exception {
        mvc.perform(post("/api/ordens-servico").with(jwtAtendente())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"documentoCliente\":\"" + documento
                                + "\",\"relatoProblema\":\"Barulho na suspensão dianteira\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem", containsString("informe veiculoId ou placaVeiculo")));
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
}