package br.com.fiap.oficina_mecanica.estoque.infrastructure.controller;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class EstoqueControllerIntegrationTest {

    @Autowired
    private MockMvc mvc;

    private static RequestPostProcessor jwtAtendente() {
        return jwt().authorities(new SimpleGrantedAuthority("ROLE_ATENDENTE"));
    }

    private String cadastrar(String nome, String valor) throws Exception {
        String resposta = mvc.perform(post("/api/estoque").with(jwtAtendente())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nomePeca":"%s","valorUnitario":%s,"quantidadeDisponivel":5}""".formatted(nome, valor)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(resposta, "$.id");
    }

    @Test
    void listaPecasFiltrandoPorNome() throws Exception {
        cadastrar("Pastilha de freio teste", "120.00");
        cadastrar("Vela de ignicao teste", "35.00");

        mvc.perform(get("/api/estoque").param("nomePeca", "pastilha de freio teste").with(jwtAtendente()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conteudo.length()").value(1))
                .andExpect(jsonPath("$.conteudo[0].nomePeca").value("PASTILHA DE FREIO TESTE"))
                .andExpect(jsonPath("$.totalElementos").value(1));
    }

    @Test
    void listarComTamanhoInvalidoRetorna400() throws Exception {
        mvc.perform(get("/api/estoque").param("tamanho", "0").with(jwtAtendente()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void atualizaPeca() throws Exception {
        String id = cadastrar("Correia dentada teste", "200.00");

        mvc.perform(put("/api/estoque/{id}", id).with(jwtAtendente())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nomePeca":"correia dentada reforcada teste","valorUnitario":250.00}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nomePeca").value("CORREIA DENTADA REFORCADA TESTE"))
                .andExpect(jsonPath("$.valorUnitario").value(250.00))
                .andExpect(jsonPath("$.quantidadeDisponivel").value(5));
    }

    @Test
    void atualizarSemNomeRetorna400() throws Exception {
        String id = cadastrar("Amortecedor teste", "300.00");

        mvc.perform(put("/api/estoque/{id}", id).with(jwtAtendente())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nomePeca":"","valorUnitario":10.00}"""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void excluiPecaERetorna204() throws Exception {
        String id = cadastrar("Lampada teste", "15.00");

        mvc.perform(delete("/api/estoque/{id}", id).with(jwtAtendente()))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/estoque/{id}", id).with(jwtAtendente()))
                .andExpect(status().isNotFound());
    }

    @Test
    void excluirPecaInexistenteRetorna404() throws Exception {
        mvc.perform(delete("/api/estoque/{id}", UUID.randomUUID()).with(jwtAtendente()))
                .andExpect(status().isNotFound());
    }

    @Test
    void semTokenRetorna401() throws Exception {
        mvc.perform(get("/api/estoque"))
                .andExpect(status().isUnauthorized());
    }
}
