package br.com.fiap.oficina_mecanica.cliente.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class EnderecoTest {

    private static final Estado ESTADO = Estado.SP;

    private static final String RUA = "Rua das Flores";
    private static final String NUMERO = "123";
    private static final String COMPLEMENTO = "Apto 45";
    private static final String BAIRRO = "Centro";
    private static final String CIDADE = "São Paulo";
    private static final String CEP = "01001000";

    private Endereco criarEndereco(String rua, String numero, String complemento,
                                   String bairro, String cidade, Estado estado, String cep) {
        return new Endereco(rua, numero, complemento, bairro, cidade, estado, cep);
    }

    private Endereco criarEnderecoValido() {
        return criarEndereco(RUA, NUMERO, COMPLEMENTO, BAIRRO, CIDADE, ESTADO, CEP);
    }

    @Nested
    @DisplayName("Criação com dados válidos")
    class CriacaoValida {

        @Test
        @DisplayName("Deve criar endereço e expor todos os valores pelos getters")
        void deveCriarEnderecoValido() {
            Endereco endereco = criarEnderecoValido();

            assertEquals("Rua Das Flores", endereco.getRua());
            assertEquals("123", endereco.getNumero());
            assertEquals("Apto 45", endereco.getComplemento());
            assertEquals("Centro", endereco.getBairro());
            assertEquals("São Paulo", endereco.getCidade());
            assertEquals(ESTADO, endereco.getEstado());
            assertEquals("01001000", endereco.getCep());
        }

        @Test
        @DisplayName("Deve remover espaços nas extremidades dos campos")
        void deveRemoverEspacosDasExtremidades() {
            Endereco endereco = criarEndereco("  rua das flores  ", "  10  ", "  fundos  ",
                    "  centro  ", "  são paulo  ", ESTADO, "  01001000  ");

            assertEquals("Rua Das Flores", endereco.getRua());
            assertEquals("10", endereco.getNumero());
            assertEquals("fundos", endereco.getComplemento());
            assertEquals("Centro", endereco.getBairro());
            assertEquals("São Paulo", endereco.getCidade());
            assertEquals("01001000", endereco.getCep());
        }

        @Test
        @DisplayName("Deve deixar a primeira letra de cada palavra maiúscula e o resto minúscula")
        void deveNormalizarCapitalizacao() {
            Endereco endereco = criarEndereco("RUA DAS FLORES", NUMERO, null,
                    "jARDIM aMÉRICA", "SÃO paulo", ESTADO, CEP);

            assertEquals("Rua Das Flores", endereco.getRua());
            assertEquals("Jardim América", endereco.getBairro());
            assertEquals("São Paulo", endereco.getCidade());
        }

        @Test
        @DisplayName("Deve ignorar espaços múltiplos entre as palavras")
        void deveIgnorarEspacosMultiplosEntrePalavras() {
            Endereco endereco = criarEndereco("rua   das    flores", NUMERO, null,
                    BAIRRO, CIDADE, ESTADO, CEP);

            assertEquals("Rua Das Flores", endereco.getRua());
        }

        @Test
        @DisplayName("Deve tratar texto com uma única palavra")
        void deveTratarUmaUnicaPalavra() {
            Endereco endereco = criarEndereco("avenida", NUMERO, null,
                    "centro", "campinas", ESTADO, CEP);

            assertEquals("Avenida", endereco.getRua());
            assertEquals("Centro", endereco.getBairro());
            assertEquals("Campinas", endereco.getCidade());
        }
    }

    @Nested
    @DisplayName("Complemento")
    class Complemento {

        @Test
        @DisplayName("Deve aceitar complemento nulo")
        void deveAceitarComplementoNulo() {
            Endereco endereco = criarEndereco(RUA, NUMERO, null, BAIRRO, CIDADE, ESTADO, CEP);

            assertNull(endereco.getComplemento());
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   ", "\t"})
        @DisplayName("Deve transformar complemento em branco em nulo")
        void deveTransformarComplementoEmBrancoEmNulo(String complemento) {
            Endereco endereco = criarEndereco(RUA, NUMERO, complemento, BAIRRO, CIDADE, ESTADO, CEP);

            assertNull(endereco.getComplemento());
        }

        @Test
        @DisplayName("Deve manter a capitalização original do complemento")
        void deveManterCapitalizacaoDoComplemento() {
            Endereco endereco = criarEndereco(RUA, NUMERO, "  Bloco B, APTO 12  ", BAIRRO, CIDADE, ESTADO, CEP);

            assertEquals("Bloco B, APTO 12", endereco.getComplemento());
        }
    }

    @Nested
    @DisplayName("CEP")
    class Cep {

        @Test
        @DisplayName("Deve remover caracteres não numéricos do CEP")
        void deveRemoverMascaraDoCep() {
            Endereco endereco = criarEndereco(RUA, NUMERO, null, BAIRRO, CIDADE, ESTADO, "01001-000");

            assertEquals("01001000", endereco.getCep());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t"})
        @DisplayName("Deve lançar exceção quando o CEP for nulo ou em branco")
        void deveLancarExcecaoParaCepNuloOuEmBranco(String cep) {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> criarEndereco(RUA, NUMERO, COMPLEMENTO, BAIRRO, CIDADE, ESTADO, cep));

            assertEquals("O(a) cep do endereço é inválido(a)", ex.getMessage());
        }

        @ParameterizedTest
        @ValueSource(strings = {"1234567", "123456789", "abcdefgh", "12-34"})
        @DisplayName("Deve lançar exceção quando o CEP não tiver 8 dígitos")
        void deveLancarExcecaoParaCepComTamanhoInvalido(String cep) {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> criarEndereco(RUA, NUMERO, COMPLEMENTO, BAIRRO, CIDADE, ESTADO, cep));

            assertEquals("O CEP do endereço é inválido", ex.getMessage());
        }

        @ParameterizedTest
        @ValueSource(strings = {"1234567a8", "0100100A0", "01001-00B0"})
        @DisplayName("Deve lançar exceção quando o CEP tiver 8 dígitos mas contiver letras")
        void deveLancarExcecaoParaCepComLetras(String cep) {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> criarEndereco(RUA, NUMERO, COMPLEMENTO, BAIRRO, CIDADE, ESTADO, cep));

            assertEquals("O CEP do endereço é inválido", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("Estado")
    class EstadoTeste {

        @Test
        @DisplayName("Deve lançar exceção quando o estado for nulo")
        void deveLancarExcecaoParaEstadoNulo() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> criarEndereco(RUA, NUMERO, COMPLEMENTO, BAIRRO, CIDADE, null, CEP));

            assertEquals("O Estado do endereço é inválido", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("Campos de texto obrigatórios")
    class CamposObrigatorios {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t"})
        @DisplayName("Deve lançar exceção quando a rua for nula ou em branco")
        void deveLancarExcecaoParaRuaInvalida(String rua) {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> criarEndereco(rua, NUMERO, COMPLEMENTO, BAIRRO, CIDADE, ESTADO, CEP));

            assertEquals("O(a) rua do endereço é inválido(a)", ex.getMessage());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t"})
        @DisplayName("Deve lançar exceção quando o número for nulo ou em branco")
        void deveLancarExcecaoParaNumeroInvalido(String numero) {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> criarEndereco(RUA, numero, COMPLEMENTO, BAIRRO, CIDADE, ESTADO, CEP));

            assertEquals("O(a) número do endereço é inválido(a)", ex.getMessage());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t"})
        @DisplayName("Deve lançar exceção quando o bairro for nulo ou em branco")
        void deveLancarExcecaoParaBairroInvalido(String bairro) {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> criarEndereco(RUA, NUMERO, COMPLEMENTO, bairro, CIDADE, ESTADO, CEP));

            assertEquals("O(a) bairro do endereço é inválido(a)", ex.getMessage());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t"})
        @DisplayName("Deve lançar exceção quando a cidade for nula ou em branco")
        void deveLancarExcecaoParaCidadeInvalida(String cidade) {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> criarEndereco(RUA, NUMERO, COMPLEMENTO, BAIRRO, cidade, ESTADO, CEP));

            assertEquals("O(a) cidade do endereço é inválido(a)", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("converterParaString")
    class ConverterParaString {

        @Test
        @DisplayName("Deve converter para String incluindo o complemento")
        void deveConverterComComplemento() {
            Endereco endereco = criarEnderecoValido();

            String esperado = String.format(
                    "Rua: Rua Das Flores, Número: 123, Complemento: Apto 45, Bairro: Centro, "
                            + "Cidade: São Paulo, Estado: %s, CEP: 01001000", ESTADO);

            assertEquals(esperado, endereco.converterParaString());
        }

        @Test
        @DisplayName("Deve converter para String com complemento vazio quando for nulo")
        void deveConverterSemComplemento() {
            Endereco endereco = criarEndereco(RUA, NUMERO, null, BAIRRO, CIDADE, ESTADO, CEP);

            String esperado = String.format(
                    "Rua: Rua Das Flores, Número: 123, Complemento: , Bairro: Centro, "
                            + "Cidade: São Paulo, Estado: %s, CEP: 01001000", ESTADO);

            assertEquals(esperado, endereco.converterParaString());
        }
    }
}