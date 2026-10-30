package br.com.fiap.oficina_mecanica.ordemservico;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;
import java.util.Map;
import java.util.UUID;

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
@ExtendWith(OutputCaptureExtension.class)
class OrdemServicoIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbc;

    private UUID clienteId;
    private UUID veiculoId;
    private String documentoCliente;

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
        documentoCliente = documento;
        String placa = clienteId.toString().substring(0, 7).toUpperCase();
        jdbc.update("INSERT INTO clientes (id, nome, documento, email, rua, numero, bairro, cidade, cep, estado) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                clienteId, "Cliente de teste", documento, "cliente@exemplo.com", "Rua das Flores", "123", "Centro", "São Paulo", "01001000", "SP");
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

    private String gerarOrcamento(String ordemServicoId) throws Exception {
        acao(ordemServicoId, "iniciar-diagnostico", jwtAtendente(), 204);
        String resposta = mvc.perform(post("/api/orcamentos").with(jwtAtendente())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ordemServicoId\":\"" + ordemServicoId + "\",\"dataValidade\":\"2099-12-31T23:59:59\",\"servicos\":[{\"catalogoServicoId\":\""
                                + UUID.randomUUID() + "\",\"descricao\":\"Troca de óleo\",\"valorMaoDeObra\":200.00}],"
                                + "\"pecas\":[]}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(resposta, "$.id");
    }

    private String codigoAcesso(String ordemServicoId) throws Exception {
        return jdbc.queryForObject("SELECT codigo_acesso FROM ordens_servico WHERE id = ?", String.class,
                UUID.fromString(ordemServicoId));
    }

    private String corpoConsulta(String cpf, String codigoAcesso) {
        return "{\"cpf\":\"" + cpf + "\",\"codigoAcesso\":\"" + codigoAcesso + "\"}";
    }

    @Test
    void criaOrdemDeServicoEOrcamentoEAprovaOrcamento() throws Exception {
        String ordemServicoId = criarOrdemServico();
        acao(ordemServicoId, "iniciar-diagnostico", jwtAtendente(), 204);

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
    void adminCriaOrdemDeServico() throws Exception {
        mvc.perform(post("/api/ordens-servico").with(jwtAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoOrdemServico(clienteId, veiculoId, "Barulho na suspensão dianteira")))
                .andExpect(status().isCreated());
    }

    @Test
    void criarSemTokenVolta401() throws Exception {
        mvc.perform(post("/api/ordens-servico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoOrdemServico(clienteId, veiculoId, "Barulho na suspensão dianteira")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void criarComPapelSemPermissaoVolta403() throws Exception {
        mvc.perform(post("/api/ordens-servico").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CLIENTE")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoOrdemServico(clienteId, veiculoId, "Barulho na suspensão dianteira")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.erro").value("Acesso Negado"));
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

    @Test
    void adminDetalhaOrdemComClienteVeiculoEOrcamentos() throws Exception {
        String ordemServicoId = criarOrdemServico();
        String orcamentoId = gerarOrcamento(ordemServicoId);

        mvc.perform(get("/api/ordens-servico/" + ordemServicoId).with(jwtAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ordemServicoId))
                .andExpect(jsonPath("$.status").value("AGUARDANDO_APROVACAO"))
                .andExpect(jsonPath("$.relatoProblema").value("Barulho na suspensão dianteira"))
                .andExpect(jsonPath("$.codigoAcesso").doesNotExist())
                .andExpect(jsonPath("$.cliente.id").value(clienteId.toString()))
                .andExpect(jsonPath("$.cliente.nome").value("Cliente de teste"))
                .andExpect(jsonPath("$.cliente.documento").value(documentoCliente))
                .andExpect(jsonPath("$.veiculo.id").value(veiculoId.toString()))
                .andExpect(jsonPath("$.veiculo.ano").value(2022))
                .andExpect(jsonPath("$.orcamentos.length()").value(1))
                .andExpect(jsonPath("$.orcamentos[0].id").value(orcamentoId))
                .andExpect(jsonPath("$.orcamentos[0].status").value("PENDENTE"))
                .andExpect(jsonPath("$.orcamentos[0].servicos[0].descricao").value("Troca de óleo"));
    }

    @Test
    void atendenteDetalhaOrdemSemOrcamentos() throws Exception {
        String ordemServicoId = criarOrdemServico();

        mvc.perform(get("/api/ordens-servico/" + ordemServicoId).with(jwtAtendente()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RECEBIDA"))
                .andExpect(jsonPath("$.orcamentos").isEmpty());
    }

    @Test
    void detalheMantemOrcamentosJaDecididos() throws Exception {
        String ordemServicoId = criarOrdemServico();
        String orcamentoId = gerarOrcamento(ordemServicoId);
        mvc.perform(patch("/api/orcamentos/" + orcamentoId + "/aprovar").with(jwtAtendente()))
                .andExpect(status().isOk());

        mvc.perform(get("/api/ordens-servico/" + ordemServicoId).with(jwtAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APROVADA"))
                .andExpect(jsonPath("$.orcamentos[0].status").value("APROVADO"));
    }

    @Test
    void detalheDeOrdemInexistenteVolta404() throws Exception {
        mvc.perform(get("/api/ordens-servico/" + UUID.randomUUID()).with(jwtAtendente()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Ordem de Serviço não encontrada."));
    }

    @Test
    void detalheComIdMalFormadoVolta400() throws Exception {
        mvc.perform(get("/api/ordens-servico/nao-e-uuid").with(jwtAtendente()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Requisição Inválida"));
    }

    @Test
    void detalheSemTokenVolta401() throws Exception {
        mvc.perform(get("/api/ordens-servico/" + UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void detalheComPapelSemPermissaoVolta403() throws Exception {
        mvc.perform(get("/api/ordens-servico/" + UUID.randomUUID())
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CLIENTE"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void clienteConsultaSuaOrdemSemToken() throws Exception {
        String ordemServicoId = criarOrdemServico();
        String orcamentoId = gerarOrcamento(ordemServicoId);
        String codigo = codigoAcesso(ordemServicoId);
        String cpfFormatado = documentoCliente.substring(0, 3) + "." + documentoCliente.substring(3, 6) + "."
                + documentoCliente.substring(6, 9) + "-" + documentoCliente.substring(9);

        mvc.perform(post("/api/publico/ordens-servico/" + ordemServicoId + "/consultar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoConsulta(cpfFormatado, codigo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ordemServicoId))
                .andExpect(jsonPath("$.status").value("AGUARDANDO_APROVACAO"))
                .andExpect(jsonPath("$.veiculo.marca").value("Fiat"))
                .andExpect(jsonPath("$.veiculo.modelo").value("Argo"))
                .andExpect(jsonPath("$.ultimoOrcamento.id").value(orcamentoId))
                .andExpect(jsonPath("$.ultimoOrcamento.status").value("PENDENTE"))
                .andExpect(jsonPath("$.ultimoOrcamento.valorTotal").value(200.00))
                .andExpect(jsonPath("$.ultimoOrcamento.dataCriacao").isNotEmpty())
                .andExpect(jsonPath("$.ultimoOrcamento.dataValidade").isNotEmpty())
                .andExpect(jsonPath("$.ultimoOrcamento.servicos").doesNotExist())
                .andExpect(jsonPath("$.orcamentos").doesNotExist())
                .andExpect(jsonPath("$.codigoAcesso").doesNotExist())
                .andExpect(jsonPath("$.cliente").doesNotExist());
    }

    @Test
    void consultaDoClienteMostraOrcamentoJaDecidido() throws Exception {
        String ordemServicoId = criarOrdemServico();
        String orcamentoId = gerarOrcamento(ordemServicoId);
        String codigo = codigoAcesso(ordemServicoId);
        mvc.perform(patch("/api/orcamentos/" + orcamentoId + "/aprovar").with(jwtAtendente()))
                .andExpect(status().isOk());

        mvc.perform(post("/api/publico/ordens-servico/" + ordemServicoId + "/consultar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoConsulta(documentoCliente, codigo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APROVADA"))
                .andExpect(jsonPath("$.ultimoOrcamento.id").value(orcamentoId))
                .andExpect(jsonPath("$.ultimoOrcamento.status").value("APROVADO"));
    }

    @Test
    void consultaComCodigoDeAcessoErradoVolta422() throws Exception {
        String ordemServicoId = criarOrdemServico();

        mvc.perform(post("/api/publico/ordens-servico/" + ordemServicoId + "/consultar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoConsulta(documentoCliente, "ZZZZZZ")))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.mensagem").value("Credenciais inválidas"));
    }

    @Test
    void consultaComCpfDeOutroClienteVolta422() throws Exception {
        String ordemServicoId = criarOrdemServico();
        String codigo = codigoAcesso(ordemServicoId);

        mvc.perform(post("/api/publico/ordens-servico/" + ordemServicoId + "/consultar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoConsulta("00000000000", codigo)))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.mensagem").value("Credenciais inválidas"));
    }

    @Test
    void consultaSemCamposObrigatoriosVolta400() throws Exception {
        mvc.perform(post("/api/publico/ordens-servico/" + UUID.randomUUID() + "/consultar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Dados Inválidos"))
                .andExpect(jsonPath("$.mensagem")
                        .value("codigoAcesso: O código de acesso é obrigatório.; cpf: O CPF é obrigatório."));
    }

    @Test
    void consultaDeOrdemInexistenteVolta404() throws Exception {
        mvc.perform(post("/api/publico/ordens-servico/" + UUID.randomUUID() + "/consultar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoConsulta(documentoCliente, "ABC123")))
                .andExpect(status().isNotFound());
    }

    @Test
    void consultaDoClienteNaoAceitaGet() throws Exception {
        mvc.perform(get("/api/publico/ordens-servico/" + UUID.randomUUID() + "/consultar"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void cancelaOrdemRecebida() throws Exception {
        String ordemServicoId = criarOrdemServico();

        mvc.perform(patch("/api/ordens-servico/" + ordemServicoId + "/cancelar").with(jwtAtendente()))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/ordens-servico/" + ordemServicoId).with(jwtAdmin()))
                .andExpect(jsonPath("$.status").value("CANCELADA"));
    }

    @Test
    void cancelarComOrcamentoPendenteRejeitaOOrcamento() throws Exception {
        String ordemServicoId = criarOrdemServico();
        gerarOrcamento(ordemServicoId);

        mvc.perform(patch("/api/ordens-servico/" + ordemServicoId + "/cancelar").with(jwtAdmin()))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/ordens-servico/" + ordemServicoId).with(jwtAdmin()))
                .andExpect(jsonPath("$.status").value("CANCELADA"))
                .andExpect(jsonPath("$.orcamentos[0].status").value("REJEITADO"));
    }

    @Test
    void cancelarDuasVezesVolta422() throws Exception {
        String ordemServicoId = criarOrdemServico();
        mvc.perform(patch("/api/ordens-servico/" + ordemServicoId + "/cancelar").with(jwtAtendente()))
                .andExpect(status().isNoContent());

        mvc.perform(patch("/api/ordens-servico/" + ordemServicoId + "/cancelar").with(jwtAtendente()))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.erro").value("Violação de Regra de Negócio"));
    }

    @Test
    void cancelarOrdemComOrcamentoAprovadoVolta422() throws Exception {
        String ordemServicoId = criarOrdemServico();
        String orcamentoId = gerarOrcamento(ordemServicoId);
        mvc.perform(patch("/api/orcamentos/" + orcamentoId + "/aprovar").with(jwtAtendente()))
                .andExpect(status().isOk());

        mvc.perform(patch("/api/ordens-servico/" + ordemServicoId + "/cancelar").with(jwtAtendente()))
                .andExpect(status().is(422));

        mvc.perform(get("/api/ordens-servico/" + ordemServicoId).with(jwtAdmin()))
                .andExpect(jsonPath("$.status").value("APROVADA"))
                .andExpect(jsonPath("$.orcamentos[0].status").value("APROVADO"));
    }

    @Test
    void cancelarOrdemInexistenteVolta404() throws Exception {
        mvc.perform(patch("/api/ordens-servico/" + UUID.randomUUID() + "/cancelar").with(jwtAtendente()))
                .andExpect(status().isNotFound());
    }

    @Test
    void cancelarSemTokenVolta401EComPapelSemPermissaoVolta403() throws Exception {
        String url = "/api/ordens-servico/" + UUID.randomUUID() + "/cancelar";

        mvc.perform(patch(url)).andExpect(status().isUnauthorized());
        mvc.perform(patch(url).with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CLIENTE"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void criarSemTokenVolta401EComPapelSemPermissaoVolta403() throws Exception {
        String corpo = corpoOrdemServico(clienteId, veiculoId, "Barulho na suspensão dianteira");

        mvc.perform(post("/api/ordens-servico").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/ordens-servico").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CLIENTE")))
                        .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.erro").value("Acesso Negado"));
    }

    @Test
    void adminTambemPodeCriarOrdemDeServico() throws Exception {
        mvc.perform(post("/api/ordens-servico").with(jwtAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoOrdemServico(clienteId, veiculoId, "Barulho na suspensão dianteira")))
                .andExpect(status().isCreated());
    }

    private String criarOrdemAprovada() throws Exception {
        String ordemServicoId = criarOrdemServico();
        String orcamentoId = gerarOrcamento(ordemServicoId);
        mvc.perform(patch("/api/orcamentos/" + orcamentoId + "/aprovar").with(jwtAtendente()))
                .andExpect(status().isOk());
        return ordemServicoId;
    }

    private void acao(String ordemServicoId, String caminho, RequestPostProcessor usuario, int statusEsperado)
            throws Exception {
        mvc.perform(patch("/api/ordens-servico/" + ordemServicoId + "/" + caminho).with(usuario))
                .andExpect(status().is(statusEsperado));
    }

    @Test
    void registraHistoricoDeCadaTransicaoDeStatusSemDuplicar() throws Exception {
        String ordemServicoId = criarOrdemAprovada();
        acao(ordemServicoId, "iniciar-execucao", jwtAtendente(), 204);
        acao(ordemServicoId, "finalizar", jwtAtendente(), 204);
        acao(ordemServicoId, "entregar", jwtAtendente(), 204);

        List<Map<String, Object>> historico = jdbc.queryForList(
                "SELECT status_anterior, status_novo FROM historico_status_ordem_servico "
                        + "WHERE ordem_servico_id = ? ORDER BY data_transicao, id", UUID.fromString(ordemServicoId));

        assertThat(historico).extracting(h -> h.get("status_novo")).containsExactly(
                "RECEBIDA", "EM_DIAGNOSTICO", "AGUARDANDO_APROVACAO", "APROVADA", "EM_EXECUCAO", "FINALIZADA",
                "ENTREGUE");
        assertThat(historico.get(0).get("status_anterior")).isNull();
        assertThat(historico.get(6).get("status_anterior")).isEqualTo("FINALIZADA");
    }

    @Test
    void percorreOCicloCompletoDaOrdemAteAEntrega() throws Exception {
        String ordemServicoId = criarOrdemAprovada();

        acao(ordemServicoId, "iniciar-execucao", jwtAtendente(), 204);
        mvc.perform(get("/api/ordens-servico/" + ordemServicoId).with(jwtAdmin()))
                .andExpect(jsonPath("$.status").value("EM_EXECUCAO"))
                .andExpect(jsonPath("$.dataConclusao").doesNotExist());

        acao(ordemServicoId, "finalizar", jwtAtendente(), 204);
        mvc.perform(get("/api/ordens-servico/" + ordemServicoId).with(jwtAdmin()))
                .andExpect(jsonPath("$.status").value("FINALIZADA"))
                .andExpect(jsonPath("$.dataConclusao").isNotEmpty());

        acao(ordemServicoId, "entregar", jwtAtendente(), 204);
        mvc.perform(get("/api/ordens-servico/" + ordemServicoId).with(jwtAdmin()))
                .andExpect(jsonPath("$.status").value("ENTREGUE"))
                .andExpect(jsonPath("$.dataConclusao").isNotEmpty());
    }

    @Test
    void finalizarAvisaOClienteQueOVeiculoEstaProntoParaRetirada(CapturedOutput saida) throws Exception {
        String ordemServicoId = criarOrdemAprovada();
        acao(ordemServicoId, "iniciar-execucao", jwtAtendente(), 204);
        assertThat(saida.getOut()).doesNotContain("Veículo pronto para retirada");

        acao(ordemServicoId, "finalizar", jwtAtendente(), 204);

        assertThat(saida.getOut())
                .contains("Veículo pronto para retirada")
                .contains("Argo (placa ");
    }

    @Test
    void finalizarComFalhaNaoAvisaOCliente(CapturedOutput saida) throws Exception {
        String ordemServicoId = criarOrdemAprovada();

        acao(ordemServicoId, "finalizar", jwtAtendente(), 422);

        assertThat(saida.getOut()).doesNotContain("Veículo pronto para retirada");
    }

    @Test
    void aprovarOrcamentoAvisaOClienteParaAcompanharAOrdem(CapturedOutput saida) throws Exception {
        String ordemServicoId = criarOrdemServico();
        String orcamentoId = gerarOrcamento(ordemServicoId);
        assertThat(saida.getOut()).doesNotContain("Or\u00e7amento aprovado");

        mvc.perform(patch("/api/orcamentos/" + orcamentoId + "/aprovar").with(jwtAtendente()))
                .andExpect(status().isOk());

        assertThat(saida.getOut())
                .contains("Or\u00e7amento aprovado")
                .contains("/api/publico/ordens-servico/" + ordemServicoId + "/consultar");
    }

    @Test
    void aprovacaoPeloClienteTambemAvisa(CapturedOutput saida) throws Exception {
        String ordemServicoId = criarOrdemServico();
        String orcamentoId = gerarOrcamento(ordemServicoId);
        String codigo = codigoAcesso(ordemServicoId);

        mvc.perform(post("/api/publico/orcamentos/" + orcamentoId + "/aprovar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoConsulta(documentoCliente, codigo)))
                .andExpect(status().isOk());

        assertThat(saida.getOut()).contains("Or\u00e7amento aprovado");
    }

    @Test
    void clienteConsultaOrcamentoSemTokenComCpfECodigoDeAcesso() throws Exception {
        String ordemServicoId = criarOrdemServico();
        String orcamentoId = gerarOrcamento(ordemServicoId);
        String codigo = codigoAcesso(ordemServicoId);

        mvc.perform(post("/api/publico/orcamentos/" + orcamentoId + "/consultar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoConsulta(documentoCliente, codigo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orcamentoId))
                .andExpect(jsonPath("$.status").value("PENDENTE"));
    }

    @Test
    void clienteNaoConsultaOrcamentoComCodigoDeAcessoErrado() throws Exception {
        String ordemServicoId = criarOrdemServico();
        String orcamentoId = gerarOrcamento(ordemServicoId);

        mvc.perform(post("/api/publico/orcamentos/" + orcamentoId + "/consultar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoConsulta(documentoCliente, "ERRADO")))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void clienteRejeitaOrcamentoSemToken(CapturedOutput saida) throws Exception {
        String ordemServicoId = criarOrdemServico();
        String orcamentoId = gerarOrcamento(ordemServicoId);
        String codigo = codigoAcesso(ordemServicoId);

        mvc.perform(post("/api/publico/orcamentos/" + orcamentoId + "/rejeitar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoConsulta(documentoCliente, codigo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJEITADO"));

        assertThat(saida.getOut()).contains("Or\u00e7amento n\u00e3o aprovado");
    }

    @Test
    void rotasAntigasDoClienteNoOrcamentoNaoExistemMais() throws Exception {
        mvc.perform(post("/api/orcamentos/" + UUID.randomUUID() + "/cliente/consultar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoConsulta(documentoCliente, "ABC123")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejeitarOrcamentoAvisaQueNaoFoiAprovado(CapturedOutput saida) throws Exception {
        String ordemServicoId = criarOrdemServico();
        String orcamentoId = gerarOrcamento(ordemServicoId);

        mvc.perform(patch("/api/orcamentos/" + orcamentoId + "/rejeitar").with(jwtAtendente()))
                .andExpect(status().isOk());

        assertThat(saida.getOut())
                .contains("Or\u00e7amento n\u00e3o aprovado")
                .doesNotContain("Or\u00e7amento aprovado");
    }

    @Test
    void aprovarComTodosOsItensRecusadosAvisaQueNaoFoiAprovado(CapturedOutput saida) throws Exception {
        String ordemServicoId = criarOrdemServico();
        String orcamentoId = gerarOrcamento(ordemServicoId);

        mvc.perform(patch("/api/orcamentos/" + orcamentoId + "/aprovar").with(jwtAtendente())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"servicosAprovados\":[],\"pecasAprovadas\":[]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJEITADO"));

        assertThat(saida.getOut())
                .contains("Or\u00e7amento n\u00e3o aprovado")
                .doesNotContain("Or\u00e7amento aprovado");
    }

    @Test
    void cancelarOrdemAvisaOCliente(CapturedOutput saida) throws Exception {
        String ordemServicoId = criarOrdemServico();

        acao(ordemServicoId, "cancelar", jwtAtendente(), 204);

        assertThat(saida.getOut()).contains("Ordem de servi\u00e7o cancelada");
    }

    @Test
    void cancelarOrdemQueNaoPodeSerCanceladaNaoAvisa(CapturedOutput saida) throws Exception {
        String ordemServicoId = criarOrdemAprovada();

        acao(ordemServicoId, "cancelar", jwtAtendente(), 422);

        assertThat(saida.getOut()).doesNotContain("Ordem de servi\u00e7o cancelada");
    }

    @Test
    void gerarOrcamentoAvisaOClienteDepoisDoCommit(CapturedOutput saida) throws Exception {
        String ordemServicoId = criarOrdemServico();

        gerarOrcamento(ordemServicoId);

        assertThat(saida.getOut())
                .contains("Novo or\u00e7amento gerado")
                .contains("/api/publico/orcamentos/");
        assertThat(saida.getOut().split("Novo or\u00e7amento gerado", -1)).hasSize(2);
    }

    @Test
    void criarOrdemAvisaOClienteComLinkDeAcompanhamento(CapturedOutput saida) throws Exception {
        String ordemServicoId = criarOrdemServico();

        assertThat(saida.getOut())
                .contains("Ordem de servi\u00e7o recebida")
                .contains("/api/publico/ordens-servico/" + ordemServicoId + "/consultar")
                .contains("C\u00f3digo de Acesso: ******")
                .doesNotContain(codigoAcesso(ordemServicoId));
    }

    @Test
    void criarOrdemInvalidaNaoAvisaOCliente(CapturedOutput saida) throws Exception {
        mvc.perform(post("/api/ordens-servico").with(jwtAtendente())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoOrdemServico(UUID.randomUUID(), veiculoId, "Barulho na suspensão dianteira")))
                .andExpect(status().isNotFound());

        assertThat(saida.getOut()).doesNotContain("Ordem de servi\u00e7o recebida");
    }

    @Test
    void gerarOrcamentoQueFalhaERevertidoNaoAvisaOCliente(CapturedOutput saida) throws Exception {
        String ordemServicoId = criarOrdemServico();
        acao(ordemServicoId, "iniciar-diagnostico", jwtAtendente(), 204);
        String corpo = "{\"ordemServicoId\":\"" + ordemServicoId + "\",\"dataValidade\":\"2099-12-31T23:59:59\","
                + "\"servicos\":[],\"pecas\":[{\"produtoEstoqueId\":\"" + UUID.randomUUID()
                + "\",\"nomePeca\":\"Filtro\",\"quantidade\":1,\"valorUnitario\":10.00}]}";

        mvc.perform(post("/api/orcamentos").with(jwtAtendente())
                        .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isNotFound());

        assertThat(saida.getOut()).doesNotContain("Novo or\u00e7amento gerado");
        mvc.perform(get("/api/ordens-servico/" + ordemServicoId).with(jwtAdmin()))
                .andExpect(jsonPath("$.status").value("EM_DIAGNOSTICO"));
    }

    @Test
    void iniciaODiagnosticoERegistraAtransicao() throws Exception {
        String ordemServicoId = criarOrdemServico();

        acao(ordemServicoId, "iniciar-diagnostico", jwtAtendente(), 204);

        mvc.perform(get("/api/ordens-servico/" + ordemServicoId).with(jwtAdmin()))
                .andExpect(jsonPath("$.status").value("EM_DIAGNOSTICO"));
    }

    @Test
    void iniciarDiagnosticoDuasVezesVolta422() throws Exception {
        String ordemServicoId = criarOrdemServico();

        acao(ordemServicoId, "iniciar-diagnostico", jwtAtendente(), 204);
        acao(ordemServicoId, "iniciar-diagnostico", jwtAtendente(), 422);
    }

    @Test
    void iniciarDiagnosticoDeOrdemInexistenteVolta404() throws Exception {
        acao(UUID.randomUUID().toString(), "iniciar-diagnostico", jwtAtendente(), 404);
    }

    @Test
    void gerarOrcamentoSemDiagnosticoIniciadoVolta422() throws Exception {
        String ordemServicoId = criarOrdemServico();

        mvc.perform(post("/api/orcamentos").with(jwtAtendente())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ordemServicoId\":\"" + ordemServicoId + "\",\"dataValidade\":\"2099-12-31T23:59:59\","
                                + "\"servicos\":[],\"pecas\":[]}"))
                .andExpect(status().is(422));

        mvc.perform(get("/api/ordens-servico/" + ordemServicoId).with(jwtAdmin()))
                .andExpect(jsonPath("$.status").value("RECEBIDA"));
    }

    @Test
    void aprovarOrcamentoExpiradoFalhaMasMesmoAssimAvisaQueNaoFoiAprovado(CapturedOutput saida) throws Exception {
        String ordemServicoId = criarOrdemServico();
        UUID orcamentoId = UUID.randomUUID();
        jdbc.update("UPDATE ordens_servico SET status = 'AGUARDANDO_APROVACAO' WHERE id = ?", UUID.fromString(ordemServicoId));
        jdbc.update("INSERT INTO orcamentos (id, ordem_servico_id, valor_total, status, data_criacao, data_validade) "
                        + "VALUES (?, ?, 100.00, 'PENDENTE', TIMESTAMP '2020-01-01 10:00:00', TIMESTAMP '2020-01-02 10:00:00')",
                orcamentoId, UUID.fromString(ordemServicoId));

        mvc.perform(patch("/api/orcamentos/" + orcamentoId + "/aprovar").with(jwtAtendente()))
                .andExpect(status().is(422));

        assertThat(saida.getOut())
                .contains("Or\u00e7amento n\u00e3o aprovado")
                .doesNotContain("Or\u00e7amento aprovado");
        mvc.perform(get("/api/ordens-servico/" + ordemServicoId).with(jwtAdmin()))
                .andExpect(jsonPath("$.status").value("RECUSADA"));
    }

    @Test
    void adminTambemAvancaAOrdem() throws Exception {
        String ordemServicoId = criarOrdemAprovada();

        acao(ordemServicoId, "iniciar-execucao", jwtAdmin(), 204);
        acao(ordemServicoId, "finalizar", jwtAdmin(), 204);
        acao(ordemServicoId, "entregar", jwtAdmin(), 204);
    }

    @Test
    void naoIniciaExecucaoSemAprovacaoDoOrcamento() throws Exception {
        String ordemServicoId = criarOrdemServico();
        gerarOrcamento(ordemServicoId);

        mvc.perform(patch("/api/ordens-servico/" + ordemServicoId + "/iniciar-execucao").with(jwtAtendente()))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.mensagem").value("A execu\u00e7\u00e3o s\u00f3 pode ser iniciada em uma OS aprovada."));
    }

    @Test
    void naoFinalizaOrdemQueNaoEstaEmExecucao() throws Exception {
        String ordemServicoId = criarOrdemAprovada();

        mvc.perform(patch("/api/ordens-servico/" + ordemServicoId + "/finalizar").with(jwtAtendente()))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.mensagem").value("Apenas uma OS em execu\u00e7\u00e3o pode ser finalizada."));
    }

    @Test
    void naoEntregaOrdemQueNaoFoiFinalizada() throws Exception {
        String ordemServicoId = criarOrdemAprovada();
        acao(ordemServicoId, "iniciar-execucao", jwtAtendente(), 204);

        mvc.perform(patch("/api/ordens-servico/" + ordemServicoId + "/entregar").with(jwtAtendente()))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.mensagem").value("Apenas uma OS finalizada pode ser entregue."));
    }

    @Test
    void naoRepeteATransicaoJaRealizada() throws Exception {
        String ordemServicoId = criarOrdemAprovada();
        acao(ordemServicoId, "iniciar-execucao", jwtAtendente(), 204);
        acao(ordemServicoId, "iniciar-execucao", jwtAtendente(), 422);
        acao(ordemServicoId, "finalizar", jwtAtendente(), 204);
        acao(ordemServicoId, "finalizar", jwtAtendente(), 422);
        acao(ordemServicoId, "entregar", jwtAtendente(), 204);
        acao(ordemServicoId, "entregar", jwtAtendente(), 422);
    }

    @Test
    void ordemEmExecucaoNaoPodeSerCancelada() throws Exception {
        String ordemServicoId = criarOrdemAprovada();
        acao(ordemServicoId, "iniciar-execucao", jwtAtendente(), 204);

        acao(ordemServicoId, "cancelar", jwtAtendente(), 422);
    }

    @Test
    void calculaTempoMedioEntreInicioDaExecucaoEFinalizacao() throws Exception {
        jdbc.update("DELETE FROM historico_status_ordem_servico WHERE status_novo IN ('EM_EXECUCAO', 'FINALIZADA')");
        String primeira = criarOrdemAprovada();
        String segunda = criarOrdemAprovada();
        for (String id : new String[]{primeira, segunda}) {
            acao(id, "iniciar-execucao", jwtAtendente(), 204);
            acao(id, "finalizar", jwtAtendente(), 204);
        }
        definirDuracaoDaExecucao(primeira, 3600);
        definirDuracaoDaExecucao(segunda, 7200);

        mvc.perform(get("/api/ordens-servico/metricas/tempo-execucao").with(jwtAtendente()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tempoMedioSegundos").value(5400))
                .andExpect(jsonPath("$.tempoMedio").value("PT1H30M"))
                .andExpect(jsonPath("$.totalOrdensFinalizadas").value(2));
    }

    @Test
    void tempoMedioSemOrdensFinalizadasVoltaNulo() throws Exception {
        jdbc.update("DELETE FROM historico_status_ordem_servico WHERE status_novo IN ('EM_EXECUCAO', 'FINALIZADA')");
        String emExecucao = criarOrdemAprovada();
        acao(emExecucao, "iniciar-execucao", jwtAdmin(), 204);

        mvc.perform(get("/api/ordens-servico/metricas/tempo-execucao").with(jwtAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tempoMedioSegundos").doesNotExist())
                .andExpect(jsonPath("$.tempoMedio").doesNotExist())
                .andExpect(jsonPath("$.totalOrdensFinalizadas").value(0));
    }

    @Test
    void tempoMedioExigeTokenEPapelPermitido() throws Exception {
        mvc.perform(get("/api/ordens-servico/metricas/tempo-execucao")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/ordens-servico/metricas/tempo-execucao")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CLIENTE"))))
                .andExpect(status().isForbidden());
    }

    private void definirDuracaoDaExecucao(String ordemServicoId, int segundos) {
        UUID id = UUID.fromString(ordemServicoId);
        jdbc.update("UPDATE historico_status_ordem_servico SET data_transicao = TIMESTAMP '2026-01-01 08:00:00' "
                + "WHERE ordem_servico_id = ? AND status_novo = 'EM_EXECUCAO'", id);
        jdbc.update("UPDATE historico_status_ordem_servico SET data_transicao = DATEADD('SECOND', ?, TIMESTAMP '2026-01-01 08:00:00') "
                + "WHERE ordem_servico_id = ? AND status_novo = 'FINALIZADA'", segundos, id);
    }

    @Test
    void ordemInexistenteVolta404NasAcoes() throws Exception {
        String inexistente = UUID.randomUUID().toString();

        acao(inexistente, "iniciar-diagnostico", jwtAtendente(), 404);
        acao(inexistente, "iniciar-execucao", jwtAtendente(), 404);
        acao(inexistente, "finalizar", jwtAtendente(), 404);
        acao(inexistente, "entregar", jwtAtendente(), 404);
    }

    @Test
    void acoesExigemTokenEPapelPermitido() throws Exception {
        String id = UUID.randomUUID().toString();
        RequestPostProcessor papelSemPermissao = jwt().authorities(new SimpleGrantedAuthority("ROLE_CLIENTE"));

        for (String caminho : new String[]{"iniciar-diagnostico", "iniciar-execucao", "finalizar", "entregar"}) {
            mvc.perform(patch("/api/ordens-servico/" + id + "/" + caminho)).andExpect(status().isUnauthorized());
            acao(id, caminho, papelSemPermissao, 403);
        }
    }
}