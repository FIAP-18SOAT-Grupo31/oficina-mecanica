
package br.com.fiap.oficina_mecanica.cliente.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class ClienteTest {

    private static final String CPF_VALIDO = "529.982.247-25";
    private static final String CNPJ_ALFANUMERICO_VALIDO =
            "12.ABC.345/01DE-35";
    private static final String NOME_VALIDO = "Maria Silva";
    private static final String TELEFONE_VALIDO = "(11) 98765-4321";
    private static final String EMAIL_VALIDO = "maria@email.com";

    private static final String MSG_DOCUMENTO_INVALIDO =
            "O documento do cliente é inválido";
    private static final String MSG_TELEFONE_INVALIDO =
            "O telefone do cliente é inválido";
    private static final String MSG_EMAIL_INVALIDO =
            "O e-mail do cliente é inválido";
    private static final String MSG_ENDERECO_INVALIDO =
            "O endereço do cliente é inválido";

    @Mock
    private Endereco endereco;

    @Mock
    private Endereco outroEndereco;

    private Cliente criaCliente() {
        return new Cliente(
                CPF_VALIDO,
                NOME_VALIDO,
                TELEFONE_VALIDO,
                EMAIL_VALIDO,
                endereco
        );
    }

    private static IllegalArgumentException assertInvalido(Runnable acao) {
        return assertThrows(IllegalArgumentException.class, acao::run);
    }

    // ------------------------------------------------------------------
    // Construtor - documento (CPF e CNPJ numérico/alfanumérico)
    // ------------------------------------------------------------------

    @Test
    @DisplayName("Construtor: cria o cliente normalizando os dados")
    void construtor_deveCriarClienteComDadosNormalizados() {
        Cliente cliente = new Cliente(
                "  529.982.247-25  ",
                "  Maria Silva  ",
                "(11) 98765-4321",
                "  maria@email.com  ",
                endereco
        );

        assertNotNull(cliente.getId());
        assertEquals("52998224725", cliente.getDocumento());
        assertEquals("Maria Silva", cliente.getNome());
        assertEquals("11987654321", cliente.getTelefone());
        assertEquals("maria@email.com", cliente.getEmail());
        assertSame(endereco, cliente.getEndereco());

        verifyNoInteractions(endereco);
    }

    @Test
    @DisplayName("Construtor: cada cliente recebe um id próprio")
    void construtor_deveGerarIdsDiferentes() {
        assertNotEquals(criaCliente().getId(), criaCliente().getId());
    }

    @ParameterizedTest(name = "CPF válido: {0} -> {1}")
    @CsvSource({
            "529.982.247-25, 52998224725",
            "52998224725,    52998224725",
            "000.000.037-00, 00000003700",
            "000.000.074-47, 00000007447",
            "000.000.370-01, 00000037001",
            "000.000.999-70, 00000099970"
    })
    void construtor_deveAceitarCpfValido(
            String entrada,
            String esperado
    ) {
        Cliente cliente = new Cliente(
                entrada,
                NOME_VALIDO,
                TELEFONE_VALIDO,
                EMAIL_VALIDO,
                endereco
        );

        assertEquals(esperado, cliente.getDocumento());
    }

    @ParameterizedTest(name = "CNPJ válido: {0} -> {1}")
    @CsvSource({
            "11.222.333/0001-81, 11222333000181",
            "11222333000181,     11222333000181",
            "12.ABC.345/01DE-35, 12ABC34501DE35",
            "12ABC34501DE35,     12ABC34501DE35",
            "K9.P34.Y6N3/WD26-8, K9P34Y6N3WD268",
            "ZB.6CN.6Z43/DVY0-4, ZB6CN6Z43DVY04",
            "PP.6UP.3C4D/SA71-0, PP6UP3C4DSA710",
            "J8.0U3.VH6D/VIX0-0, J80U3VH6DVIX00",
            "11444777000161,     11444777000161",
            "00000000000191,     00000000000191"
    })
    void construtor_deveAceitarCnpjValidoNumericoOuAlfanumerico(
            String entrada,
            String esperado
    ) {
        Cliente cliente = new Cliente(
                entrada,
                NOME_VALIDO,
                TELEFONE_VALIDO,
                EMAIL_VALIDO,
                endereco
        );

        assertEquals(esperado, cliente.getDocumento());
    }

    @Test
    @DisplayName(
            "Construtor: normaliza CNPJ alfanumérico com letras minúsculas"
    )
    void construtor_deveAceitarCnpjAlfanumericoComLetrasMinusculas() {
        Cliente cliente = new Cliente(
                "12.abc.345/01de-35",
                NOME_VALIDO,
                TELEFONE_VALIDO,
                EMAIL_VALIDO,
                endereco
        );

        assertEquals("12ABC34501DE35", cliente.getDocumento());
    }

    @ParameterizedTest(name = "documento inválido: {0}")
    @ValueSource(strings = {
            "529.982.247-24",
            "529.982.247-35",
            "111.111.111-11",
            "1234567890",
            "123456789012",
            "1234567890123",
            "123456789012345",
            "12.ABC.345/01DE-45",
            "12.ABC.345/01DE-36",
            "12.ABC.345/01DE-00",
            "12.ABC.345/01DE-53",
            "12.ABC.345/01DE-43",
            "K9.P34.Y6N3/WD26-9",
            "K9.P34.Y6N3/WD25-8",
            "12.ABC.345/01DF-35",
            "12.ABD.345/01DE-35",
            "13.ABC.345/01DE-35",
            "12.BAC.345/01DE-35",
            "11.222.333/0001-82",
            "11.111.111/1111-11",
            "00.000.000/0000-00",
            "12.ABC.345/01DE-3A",
            "12.ABC.345/01DE-"
    })
    void construtor_deveRejeitarDocumentoInvalido(String documento) {
        IllegalArgumentException ex = assertInvalido(
                () -> new Cliente(
                        documento,
                        NOME_VALIDO,
                        TELEFONE_VALIDO,
                        EMAIL_VALIDO,
                        endereco
                )
        );

        assertEquals(MSG_DOCUMENTO_INVALIDO, ex.getMessage());
    }

    @ParameterizedTest(name = "documento nulo/vazio: [{0}]")
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void construtor_deveRejeitarDocumentoNuloOuEmBranco(
            String documento
    ) {
        IllegalArgumentException ex = assertInvalido(
                () -> new Cliente(
                        documento,
                        NOME_VALIDO,
                        TELEFONE_VALIDO,
                        EMAIL_VALIDO,
                        endereco
                )
        );

        assertEquals(
                "O(a) documento do cliente é inválido(a).",
                ex.getMessage()
        );
    }

    // ------------------------------------------------------------------
    // Construtor - demais campos
    // ------------------------------------------------------------------

    @ParameterizedTest(name = "nome nulo/vazio: [{0}]")
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void construtor_deveRejeitarNomeNuloOuEmBranco(String nome) {
        IllegalArgumentException ex = assertInvalido(
                () -> new Cliente(
                        CPF_VALIDO,
                        nome,
                        TELEFONE_VALIDO,
                        EMAIL_VALIDO,
                        endereco
                )
        );

        assertEquals(
                "O(a) nome do cliente é inválido(a).",
                ex.getMessage()
        );
    }

    @ParameterizedTest(name = "telefone inválido: [{0}]")
    @NullAndEmptySource
    @ValueSource(strings = {
            "   ",
            "123456789",
            "123456789012",
            "telefone",
            "(11) 9876-54"
    })
    void construtor_deveRejeitarTelefoneInvalido(String telefone) {
        IllegalArgumentException ex = assertInvalido(
                () -> new Cliente(
                        CPF_VALIDO,
                        NOME_VALIDO,
                        telefone,
                        EMAIL_VALIDO,
                        endereco
                )
        );

        assertTrue(
                ex.getMessage().equals(MSG_TELEFONE_INVALIDO)
                        || ex.getMessage().equals(
                        "O(a) telefone do cliente é inválido(a)."
                )
        );
    }

    @ParameterizedTest(name = "telefone válido: {0} -> {1}")
    @CsvSource({
            "(11) 3456-7890,  1134567890",
            "(11) 98765-4321, 11987654321"
    })
    void construtor_deveAceitarTelefoneComDezOuOnzeDigitos(
            String entrada,
            String esperado
    ) {
        Cliente cliente = new Cliente(
                CPF_VALIDO,
                NOME_VALIDO,
                entrada,
                EMAIL_VALIDO,
                endereco
        );

        assertEquals(esperado, cliente.getTelefone());
    }

    @ParameterizedTest(name = "e-mail inválido: [{0}]")
    @NullAndEmptySource
    @ValueSource(strings = {
            "   ",
            "email-sem-arroba",
            "@email.com",
            "maria@",
            "maria@@email.com",
            "ma@ria@email.com",
            "maria@email",
            "maria@email.c",
            "maria@email.123",
            "maria@.com",
            "maria@email..com",
            "maria@-email.com",
            "maria@email-.com",
            "maria@email.com.",
            "maria@email_x.com",
            "maria@emaíl.com",
            ".maria@email.com",
            "maria.@email.com",
            "ma..ria@email.com",
            "ma ria@email.com",
            "maria@em ail.com",
            "João@email.com",
            "maria(comentario)@email.com",
            "<maria@email.com>",
            "maria,silva@email.com"
    })
    void construtor_deveRejeitarEmailInvalido(String email) {
        IllegalArgumentException ex = assertInvalido(
                () -> new Cliente(
                        CPF_VALIDO,
                        NOME_VALIDO,
                        TELEFONE_VALIDO,
                        email,
                        endereco
                )
        );

        assertEquals(MSG_EMAIL_INVALIDO, ex.getMessage());
    }

    @ParameterizedTest(name = "e-mail válido: {0}")
    @ValueSource(strings = {
            "maria@email.com",
            "maria.silva@email.com",
            "maria.silva+vendas@empresa.com.br",
            "o'brien@email.com",
            "user_name-1@sub.dominio.org",
            "a@b.co",
            "x@a-b.com",
            "x@123.com",
            "!#$%&'*+/=?^_`{|}~-@email.com"
    })
    void construtor_deveAceitarEmailValido(String email) {
        Cliente cliente = new Cliente(
                CPF_VALIDO,
                NOME_VALIDO,
                TELEFONE_VALIDO,
                email,
                endereco
        );

        assertEquals(email, cliente.getEmail());
    }

    @ParameterizedTest(name = "e-mail normalizado: [{0}] -> {1}")
    @CsvSource(delimiter = '|', value = {
            "  maria@email.com  | maria@email.com",
            "Maria.Silva@Email.COM | maria.silva@email.com",
            "  MARIA+Vendas@EMPRESA.com.BR  | maria+vendas@empresa.com.br"
    })
    void construtor_deveNormalizarEmailRemovendoEspacosEConvertendoParaMinusculas(
            String entrada,
            String esperado
    ) {
        Cliente cliente = new Cliente(
                CPF_VALIDO,
                NOME_VALIDO,
                TELEFONE_VALIDO,
                entrada,
                endereco
        );

        assertEquals(esperado, cliente.getEmail());
    }

    @Test
    @DisplayName("e-mail: aceita no limite de tamanho")
    void construtor_deveAceitarEmailNoLimiteDeTamanho() {
        String local64 = "a".repeat(64);
        String rotulo63 = "b".repeat(63);
        String total254 = local64
                + "@"
                + ("b".repeat(60) + ".").repeat(3)
                + "abcdef";

        assertEquals(254, total254.length());

        assertEquals(
                local64 + "@email.com",
                new Cliente(
                        CPF_VALIDO,
                        NOME_VALIDO,
                        TELEFONE_VALIDO,
                        local64 + "@email.com",
                        endereco
                ).getEmail()
        );

        assertEquals(
                "a@" + rotulo63 + ".com",
                new Cliente(
                        CPF_VALIDO,
                        NOME_VALIDO,
                        TELEFONE_VALIDO,
                        "a@" + rotulo63 + ".com",
                        endereco
                ).getEmail()
        );

        assertEquals(
                total254,
                new Cliente(
                        CPF_VALIDO,
                        NOME_VALIDO,
                        TELEFONE_VALIDO,
                        total254,
                        endereco
                ).getEmail()
        );
    }

    @Test
    @DisplayName("e-mail: rejeita acima do limite de tamanho")
    void construtor_deveRejeitarEmailAcimaDoLimiteDeTamanho() {
        String local65 = "a".repeat(65) + "@email.com";
        String rotulo64 = "a@" + "b".repeat(64) + ".com";
        String total255 = "a".repeat(64)
                + "@"
                + ("b".repeat(60) + ".").repeat(3)
                + "abcdefg";

        assertEquals(255, total255.length());

        for (String email : new String[]{
                local65,
                rotulo64,
                total255
        }) {
            IllegalArgumentException ex = assertInvalido(
                    () -> new Cliente(
                            CPF_VALIDO,
                            NOME_VALIDO,
                            TELEFONE_VALIDO,
                            email,
                            endereco
                    )
            );

            assertEquals(MSG_EMAIL_INVALIDO, ex.getMessage());
        }
    }

    @Test
    @DisplayName("Construtor: rejeita endereço nulo")
    void construtor_deveRejeitarEnderecoNulo() {
        IllegalArgumentException ex = assertInvalido(
                () -> new Cliente(
                        CPF_VALIDO,
                        NOME_VALIDO,
                        TELEFONE_VALIDO,
                        EMAIL_VALIDO,
                        null
                )
        );

        assertEquals(MSG_ENDERECO_INVALIDO, ex.getMessage());
    }

    // ------------------------------------------------------------------
    // Setters
    // ------------------------------------------------------------------

    @Test
    @DisplayName("setDocumento: atualiza com CPF e CNPJ alfanumérico válidos")
    void setDocumento_deveAtualizarComDocumentoValido() {
        Cliente cliente = criaCliente();

        cliente.setDocumento("11.222.333/0001-81");
        assertEquals("11222333000181", cliente.getDocumento());

        cliente.setDocumento(CNPJ_ALFANUMERICO_VALIDO);
        assertEquals("12ABC34501DE35", cliente.getDocumento());

        cliente.setDocumento("000.000.037-00");
        assertEquals("00000003700", cliente.getDocumento());
    }

    @ParameterizedTest(name = "setDocumento inválido: [{0}]")
    @ValueSource(strings = {
            "12.ABC.345/01DE-36",
            "529.982.247-24",
            "123"
    })
    void setDocumento_deveRejeitarDocumentoInvalidoMantendoOValorAnterior(
            String invalido
    ) {
        Cliente cliente = criaCliente();

        IllegalArgumentException ex = assertInvalido(
                () -> cliente.setDocumento(invalido)
        );

        assertEquals(MSG_DOCUMENTO_INVALIDO, ex.getMessage());
        assertEquals("52998224725", cliente.getDocumento());
    }

    @Test
    @DisplayName("setDocumento: rejeita nulo")
    void setDocumento_deveRejeitarNulo() {
        Cliente cliente = criaCliente();

        assertInvalido(() -> cliente.setDocumento(null));

        assertEquals("52998224725", cliente.getDocumento());
    }

    @Test
    @DisplayName("setEmail: atualiza removendo espaços e convertendo para minúsculas")
    void setEmail_deveAtualizarComEmailValidoNormalizado() {
        Cliente cliente = criaCliente();

        cliente.setEmail("  novo@email.com  ");
        assertEquals("novo@email.com", cliente.getEmail());

        cliente.setEmail("  Novo.Nome@Email.COM  ");
        assertEquals("novo.nome@email.com", cliente.getEmail());
    }

    @ParameterizedTest(name = "setEmail inválido: [{0}]")
    @NullAndEmptySource
    @ValueSource(strings = {
            "   ",
            "sem-arroba",
            "@email.com",
            "maria@",
            "ma@ria@email.com",
            "maria@email",
            "maria@email.c",
            "ma..ria@email.com",
            "maria@-email.com",
            "João@email.com"
    })
    void setEmail_deveRejeitarEmailInvalidoMantendoOValorAnterior(
            String invalido
    ) {
        Cliente cliente = criaCliente();

        IllegalArgumentException ex = assertInvalido(
                () -> cliente.setEmail(invalido)
        );

        assertEquals(MSG_EMAIL_INVALIDO, ex.getMessage());
        assertEquals(EMAIL_VALIDO, cliente.getEmail());
    }

    @Test
    @DisplayName("setEndereco: atualiza com endereço válido")
    void setEndereco_deveAtualizarComEnderecoValido() {
        Cliente cliente = criaCliente();

        cliente.setEndereco(outroEndereco);

        assertSame(outroEndereco, cliente.getEndereco());
    }

    @Test
    @DisplayName("setEndereco: rejeita nulo mantendo o endereço anterior")
    void setEndereco_deveRejeitarNulo() {
        Cliente cliente = criaCliente();

        IllegalArgumentException ex = assertInvalido(
                () -> cliente.setEndereco(null)
        );

        assertEquals(MSG_ENDERECO_INVALIDO, ex.getMessage());
        assertSame(endereco, cliente.getEndereco());
    }

    @Test
    @DisplayName("setTelefone: atualiza mantendo somente os dígitos")
    void setTelefone_deveAtualizarComTelefoneValido() {
        Cliente cliente = criaCliente();

        cliente.setTelefone("(21) 3456-7890");

        assertEquals("2134567890", cliente.getTelefone());
    }

    @ParameterizedTest(name = "setTelefone inválido: [{0}]")
    @NullAndEmptySource
    @ValueSource(strings = {
            "   ",
            "123456789",
            "123456789012",
            "abc"
    })
    void setTelefone_deveRejeitarTelefoneInvalidoMantendoOValorAnterior(
            String invalido
    ) {
        Cliente cliente = criaCliente();

        assertInvalido(() -> cliente.setTelefone(invalido));

        assertEquals("11987654321", cliente.getTelefone());
    }

    @Test
    @DisplayName("setNome: atualiza e remove espaços")
    void setNome_deveAtualizarComNomeValido() {
        Cliente cliente = criaCliente();

        cliente.setNome("  João Souza  ");

        assertEquals("João Souza", cliente.getNome());
    }

    @ParameterizedTest(name = "setNome inválido: [{0}]")
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void setNome_deveRejeitarNomeInvalidoMantendoOValorAnterior(
            String invalido
    ) {
        Cliente cliente = criaCliente();

        IllegalArgumentException ex = assertInvalido(
                () -> cliente.setNome(invalido)
        );

        assertEquals(
                "O(a) nome do cliente é inválido(a).",
                ex.getMessage()
        );
        assertEquals(NOME_VALIDO, cliente.getNome());
    }

    // ------------------------------------------------------------------
    // reconstituir
    // ------------------------------------------------------------------

    @Test
    @DisplayName("reconstituir: mantém o id informado e normaliza os dados")
    void reconstituir_deveManterOIdInformadoENormalizarOsDados() {
        UUID id = UUID.randomUUID();

        Cliente cliente = Cliente.reconstituir(
                id,
                "529.982.247-25",
                "  Maria Silva  ",
                "(11) 98765-4321",
                "  Maria@Email.com  ",
                endereco
        );

        assertEquals(id, cliente.getId());
        assertEquals("52998224725", cliente.getDocumento());
        assertEquals("Maria Silva", cliente.getNome());
        assertEquals("11987654321", cliente.getTelefone());
        assertEquals("maria@email.com", cliente.getEmail());
        assertSame(endereco, cliente.getEndereco());
    }

    @Test
    @DisplayName("reconstituir: rejeita id nulo")
    void reconstituir_deveRejeitarIdNulo() {
        IllegalArgumentException ex = assertInvalido(
                () -> Cliente.reconstituir(
                        null,
                        CPF_VALIDO,
                        NOME_VALIDO,
                        TELEFONE_VALIDO,
                        EMAIL_VALIDO,
                        endereco
                )
        );

        assertEquals("O ID do cliente é inválido", ex.getMessage());
    }

    @Test
    @DisplayName("reconstituir: continua validando os dados recebidos")
    void reconstituir_deveRejeitarDadosInvalidos() {
        IllegalArgumentException ex = assertInvalido(
                () -> Cliente.reconstituir(
                        UUID.randomUUID(),
                        "529.982.247-24",
                        NOME_VALIDO,
                        TELEFONE_VALIDO,
                        EMAIL_VALIDO,
                        endereco
                )
        );

        assertEquals(MSG_DOCUMENTO_INVALIDO, ex.getMessage());
    }

    // ------------------------------------------------------------------
    // atualizar
    // ------------------------------------------------------------------

    @Test
    @DisplayName("atualizar: substitui todos os dados normalizando os valores")
    void atualizar_deveSubstituirTodosOsDadosNormalizados() {
        Cliente cliente = criaCliente();
        UUID id = cliente.getId();

        cliente.atualizar(
                "  11.222.333/0001-81  ",
                "  João Souza  ",
                "(21) 3456-7890",
                "  Joao@Email.COM  ",
                outroEndereco
        );

        assertEquals(id, cliente.getId());
        assertEquals("11222333000181", cliente.getDocumento());
        assertEquals("João Souza", cliente.getNome());
        assertEquals("2134567890", cliente.getTelefone());
        assertEquals("joao@email.com", cliente.getEmail());
        assertSame(outroEndereco, cliente.getEndereco());
    }

    private void assertInalterado(Cliente cliente) {
        assertEquals("52998224725", cliente.getDocumento());
        assertEquals(NOME_VALIDO, cliente.getNome());
        assertEquals("11987654321", cliente.getTelefone());
        assertEquals(EMAIL_VALIDO, cliente.getEmail());
        assertSame(endereco, cliente.getEndereco());
    }

    @Test
    @DisplayName("atualizar: se qualquer valor for inválido, nada é alterado")
    void atualizar_naoDeveAlterarNadaQuandoAlgumValorForInvalido() {
        Cliente cliente = criaCliente();

        assertEquals(
                MSG_DOCUMENTO_INVALIDO,
                assertInvalido(() -> cliente.atualizar(
                        "529.982.247-24", "Outro Nome", "(21) 3456-7890", "outro@email.com", outroEndereco
                )).getMessage()
        );
        assertInalterado(cliente);

        assertEquals(
                MSG_TELEFONE_INVALIDO,
                assertInvalido(() -> cliente.atualizar(
                        "11.222.333/0001-81", "Outro Nome", "123", "outro@email.com", outroEndereco
                )).getMessage()
        );
        assertInalterado(cliente);

        assertEquals(
                MSG_EMAIL_INVALIDO,
                assertInvalido(() -> cliente.atualizar(
                        "11.222.333/0001-81", "Outro Nome", "(21) 3456-7890", "sem-arroba", outroEndereco
                )).getMessage()
        );
        assertInalterado(cliente);

        assertEquals(
                "O(a) nome do cliente é inválido(a).",
                assertInvalido(() -> cliente.atualizar(
                        "11.222.333/0001-81", "   ", "(21) 3456-7890", "outro@email.com", outroEndereco
                )).getMessage()
        );
        assertInalterado(cliente);

        assertEquals(
                MSG_ENDERECO_INVALIDO,
                assertInvalido(() -> cliente.atualizar(
                        "11.222.333/0001-81", "Outro Nome", "(21) 3456-7890", "outro@email.com", null
                )).getMessage()
        );
        assertInalterado(cliente);
    }
}

