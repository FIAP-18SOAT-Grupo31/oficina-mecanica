package br.com.fiap.oficina_mecanica.cliente.infrastructure;

import br.com.fiap.oficina_mecanica.cliente.domain.Endereco;
import br.com.fiap.oficina_mecanica.cliente.domain.Estado;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Executa as migrations direto no Flyway (sem subir o Spring) para garantir que a V9 funciona em
 * bancos que já têm clientes salvos antes do endereço existir.
 */
class MigracaoEnderecoClientesTest {

    private String url;
    private JdbcTemplate jdbc;

    @BeforeEach
    void criarBancoIsolado() {
        url = "jdbc:h2:mem:migracao-" + UUID.randomUUID()
                + ";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;DB_CLOSE_DELAY=-1";
        jdbc = new JdbcTemplate(new DriverManagerDataSource(url, "sa", ""));
    }

    private void migrarAte(String versao) {
        Flyway.configure()
                .dataSource(url, "sa", "")
                .locations("classpath:db/migration")
                .target(MigrationVersion.fromVersion(versao))
                .load()
                .migrate();
    }

    private String valor(String coluna, UUID clienteId) {
        return jdbc.queryForObject("SELECT " + coluna + " FROM clientes WHERE id = ?", String.class, clienteId);
    }

    @Test
    void clienteJaCadastradoRecebeEnderecoProvisorioAceitoPeloDominio() {
        migrarAte("8");
        UUID clienteId = UUID.randomUUID();
        jdbc.update("INSERT INTO clientes (id, nome, documento, email, telefone) VALUES (?, ?, ?, ?, ?)",
                clienteId, "Maria da Silva", "52998224725", "maria@exemplo.com", "11999990000");

        migrarAte("9");

        assertThat(valor("rua", clienteId)).isEqualTo("Não informado");
        assertThat(valor("numero", clienteId)).isEqualTo("S/N");
        assertThat(valor("complemento", clienteId)).isNull();
        assertThat(valor("bairro", clienteId)).isEqualTo("Não informado");
        assertThat(valor("cidade", clienteId)).isEqualTo("Não informado");
        assertThat(valor("cep", clienteId)).isEqualTo("00000000");
        assertThat(valor("estado", clienteId)).isEqualTo("SP");

        Endereco endereco = new Endereco(valor("rua", clienteId), valor("numero", clienteId),
                valor("complemento", clienteId), valor("bairro", clienteId), valor("cidade", clienteId),
                Estado.valueOf(valor("estado", clienteId)), valor("cep", clienteId));
        assertThat(endereco.getCep()).isEqualTo("00000000");
    }

    @Test
    void depoisDaMigrationOEnderecoContinuaObrigatorioExcetoOComplemento() {
        migrarAte("9");

        assertThatThrownBy(() -> jdbc.update("INSERT INTO clientes (id, nome, documento) VALUES (?, ?, ?)",
                UUID.randomUUID(), "Sem endereço", "52998224725"))
                .isInstanceOf(DataIntegrityViolationException.class);

        int inseridos = jdbc.update("INSERT INTO clientes (id, nome, documento, rua, numero, bairro, cidade, cep, "
                        + "estado) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                UUID.randomUUID(), "Sem complemento", "52998224725", "Rua das Flores", "123", "Centro",
                "São Paulo", "01001000", "SP");
        assertThat(inseridos).isEqualTo(1);
    }
}
