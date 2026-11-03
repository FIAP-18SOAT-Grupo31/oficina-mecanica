package br.com.fiap.oficina_mecanica.cliente.infrastructure;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Sobe o contexto da aplicação com um banco H2 só dos testes de cliente e veículo (os dados de outros testes de
 * integração não interferem nas listagens) e desfaz cada teste no final.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"test", "clientes-test"})
@Transactional
public @interface TesteIntegracaoCliente {
}
