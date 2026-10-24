
package br.com.fiap.oficina_mecanica.veiculo.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Year;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VeiculoTest {

    private final UUID clienteId = UUID.randomUUID();

    private Veiculo criarVeiculo() {
        return new Veiculo(
                2020, "ABC1234", "Civic", "Honda", clienteId
        );
    }

    // Construtor e getters

    @Test
    @DisplayName("Deve criar veículo com dados válidos")
    void deveCriarVeiculoComDadosValidos() {
        Veiculo veiculo = criarVeiculo();

        assertNotNull(veiculo.getId());
        assertEquals(2020, veiculo.getAnoFabricacao());
        assertEquals("ABC1234", veiculo.getPlaca());
        assertEquals("Civic", veiculo.getModelo());
        assertEquals("Honda", veiculo.getMarca());
        assertEquals(clienteId, veiculo.getClienteId());
    }

    @Test
    @DisplayName("Deve gerar IDs diferentes para veículos distintos")
    void deveGerarIdsDiferentes() {
        assertNotEquals(criarVeiculo().getId(), criarVeiculo().getId());
    }

    @Test
    @DisplayName("Deve remover espaços externos do modelo e da marca")
    void deveNormalizarModeloEMarca() {
        Veiculo veiculo = new Veiculo(
                2020, "ABC1234", "  Civic  ", "  Honda  ", clienteId
        );

        assertEquals("Civic", veiculo.getModelo());
        assertEquals("Honda", veiculo.getMarca());
    }

    // Validação de placa no construtor

    @Test
    @DisplayName("Deve aceitar placa antiga")
    void deveAceitarPlacaAntiga() {
        assertEquals("ABC1234", new Veiculo(
                2020, "ABC1234", "Civic", "Honda", clienteId
        ).getPlaca());
    }

    @Test
    @DisplayName("Deve aceitar placa antiga com hífen")
    void deveAceitarPlacaAntigaComHifen() {
        assertEquals("ABC1234", new Veiculo(
                2020, "ABC-1234", "Civic", "Honda", clienteId
        ).getPlaca());
    }

    @Test
    @DisplayName("Deve aceitar placa Mercosul")
    void deveAceitarPlacaMercosul() {
        assertEquals("ABC1D23", new Veiculo(
                2020, "ABC1D23", "Civic", "Honda", clienteId
        ).getPlaca());
    }

    @Test
    @DisplayName("Deve converter placa para maiúsculas")
    void deveNormalizarPlaca() {
        assertEquals("ABC1D23", new Veiculo(
                2020, "abc1d23", "Civic", "Honda", clienteId
        ).getPlaca());
    }

    @Test
    @DisplayName("Deve rejeitar placa com formato inválido")
    void deveRejeitarPlacaComFormatoInvalido() {
        assertThrows(IllegalArgumentException.class,
                () -> new Veiculo(
                        2020, "AB123", "Civic", "Honda", clienteId
                ));
    }

    @Test
    @DisplayName("Deve rejeitar placa com caracteres especiais")
    void deveRejeitarPlacaComCaracteresEspeciais() {
        assertThrows(IllegalArgumentException.class,
                () -> new Veiculo(
                        2020, "AB@123", "Civic", "Honda", clienteId
                ));
    }

    @Test
    @DisplayName("Deve rejeitar placa nula")
    void deveRejeitarPlacaNula() {
        assertThrows(IllegalArgumentException.class,
                () -> new Veiculo(
                        2020, null, "Civic", "Honda", clienteId
                ));
    }

    @Test
    @DisplayName("Deve rejeitar placa em branco")
    void deveRejeitarPlacaEmBranco() {
        assertThrows(IllegalArgumentException.class,
                () -> new Veiculo(
                        2020, "   ", "Civic", "Honda", clienteId
                ));
    }

    // Validação de modelo e marca

    @Test
    @DisplayName("Deve rejeitar modelo nulo")
    void deveRejeitarModeloNulo() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Veiculo(
                        2020, "ABC1234", null, "Honda", clienteId
                )
        );

        assertEquals(
                "O(a) modelo do veículo é inválido(a)",
                exception.getMessage()
        );
    }

    @Test
    @DisplayName("Deve rejeitar modelo em branco")
    void deveRejeitarModeloEmBranco() {
        assertThrows(IllegalArgumentException.class,
                () -> new Veiculo(
                        2020, "ABC1234", "   ", "Honda", clienteId
                ));
    }

    @Test
    @DisplayName("Deve rejeitar marca nula")
    void deveRejeitarMarcaNula() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Veiculo(
                        2020, "ABC1234", "Civic", null, clienteId
                )
        );

        assertEquals(
                "O(a) marca do veículo é inválido(a)",
                exception.getMessage()
        );
    }

    @Test
    @DisplayName("Deve rejeitar marca em branco")
    void deveRejeitarMarcaEmBranco() {
        assertThrows(IllegalArgumentException.class,
                () -> new Veiculo(
                        2020, "ABC1234", "Civic", "   ", clienteId
                ));
    }

    // Validação de ano

    @Test
    @DisplayName("Deve aceitar o ano mínimo de fabricação")
    void deveAceitarAnoMinimo() {
        assertEquals(1900, new Veiculo(
                1900, "ABC1234", "Civic", "Honda", clienteId
        ).getAnoFabricacao());
    }

    @Test
    @DisplayName("Deve aceitar o ano atual")
    void deveAceitarAnoAtual() {
        int anoAtual = Year.now().getValue();

        assertEquals(anoAtual, new Veiculo(
                anoAtual, "ABC1234", "Civic", "Honda", clienteId
        ).getAnoFabricacao());
    }

    @Test
    @DisplayName("Deve rejeitar ano anterior a 1900")
    void deveRejeitarAnoAnteriorAoMinimo() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Veiculo(
                        1899, "ABC1234", "Civic", "Honda", clienteId
                )
        );

        assertEquals(
                "Ano de fabricação do veículo é inválido.",
                exception.getMessage()
        );
    }

    @Test
    @DisplayName("Deve rejeitar ano futuro")
    void deveRejeitarAnoFuturo() {
        assertThrows(IllegalArgumentException.class,
                () -> new Veiculo(
                        Year.now().getValue() + 1,
                        "ABC1234", "Civic", "Honda", clienteId
                ));
    }

    // Validação de clienteId

    @Test
    @DisplayName("Deve rejeitar cliente nulo")
    void deveRejeitarClienteIdNulo() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Veiculo(
                        2020, "ABC1234", "Civic", "Honda", null
                )
        );

        assertEquals(
                "O cliente do veículo é inválido.",
                exception.getMessage()
        );
    }

    // Setter de ano

    @Test
    @DisplayName("Deve atualizar ano de fabricação")
    void deveAtualizarAnoFabricacao() {
        Veiculo veiculo = criarVeiculo();

        veiculo.setAnoFabricacao(2022);

        assertEquals(2022, veiculo.getAnoFabricacao());
    }

    @Test
    @DisplayName("Não deve atualizar ano para valor inválido")
    void naoDeveAtualizarAnoInvalido() {
        Veiculo veiculo = criarVeiculo();

        assertThrows(IllegalArgumentException.class,
                () -> veiculo.setAnoFabricacao(1899));

        assertEquals(2020, veiculo.getAnoFabricacao());
    }

    // Setter de placa

    @Test
    @DisplayName("Deve atualizar placa e normalizá-la")
    void deveAtualizarPlaca() {
        Veiculo veiculo = criarVeiculo();

        veiculo.setPlaca("abc-1234");

        assertEquals("ABC1234", veiculo.getPlaca());
    }

    @Test
    @DisplayName("Não deve atualizar placa com formato inválido")
    void naoDeveAtualizarPlacaInvalida() {
        Veiculo veiculo = criarVeiculo();

        assertThrows(IllegalArgumentException.class,
                () -> veiculo.setPlaca("AB123"));

        assertEquals("ABC1234", veiculo.getPlaca());
    }

    @Test
    @DisplayName("Não deve atualizar placa com caracteres especiais")
    void naoDeveAtualizarPlacaComCaracteresEspeciais() {
        Veiculo veiculo = criarVeiculo();

        assertThrows(IllegalArgumentException.class,
                () -> veiculo.setPlaca("AB@123"));

        assertEquals("ABC1234", veiculo.getPlaca());
    }

    // Setter de modelo

    @Test
    @DisplayName("Deve atualizar modelo")
    void deveAtualizarModelo() {
        Veiculo veiculo = criarVeiculo();

        veiculo.setModelo("  Corolla  ");

        assertEquals("Corolla", veiculo.getModelo());
    }

    @Test
    @DisplayName("Não deve atualizar modelo inválido")
    void naoDeveAtualizarModeloInvalido() {
        Veiculo veiculo = criarVeiculo();

        assertThrows(IllegalArgumentException.class,
                () -> veiculo.setModelo(" "));

        assertEquals("Civic", veiculo.getModelo());
    }

    // Setter de marca

    @Test
    @DisplayName("Deve atualizar marca")
    void deveAtualizarMarca() {
        Veiculo veiculo = criarVeiculo();

        veiculo.setMarca("  Toyota  ");

        assertEquals("Toyota", veiculo.getMarca());
    }

    @Test
    @DisplayName("Não deve atualizar marca inválida")
    void naoDeveAtualizarMarcaInvalida() {
        Veiculo veiculo = criarVeiculo();

        assertThrows(IllegalArgumentException.class,
                () -> veiculo.setMarca(" "));

        assertEquals("Honda", veiculo.getMarca());
    }

    // Setter de clienteId

    @Test
    @DisplayName("Deve atualizar cliente")
    void deveAtualizarCliente() {
        Veiculo veiculo = criarVeiculo();
        UUID novoClienteId = UUID.randomUUID();

        veiculo.setClienteId(novoClienteId);

        assertEquals(novoClienteId, veiculo.getClienteId());
    }

    @Test
    @DisplayName("Não deve atualizar cliente para nulo")
    void naoDeveAtualizarClienteNulo() {
        Veiculo veiculo = criarVeiculo();

        assertThrows(IllegalArgumentException.class,
                () -> veiculo.setClienteId(null));

        assertEquals(clienteId, veiculo.getClienteId());
    }

    // reconstituir

    @Test
    @DisplayName("reconstituir: mantém o id informado e normaliza os dados")
    void reconstituirDeveManterOIdENormalizar() {
        UUID id = UUID.randomUUID();

        Veiculo veiculo = Veiculo.reconstituir(id, 2020, "abc-1234", "  Civic  ", "  Honda  ", clienteId);

        assertEquals(id, veiculo.getId());
        assertEquals("ABC1234", veiculo.getPlaca());
        assertEquals("Civic", veiculo.getModelo());
        assertEquals("Honda", veiculo.getMarca());
        assertEquals(2020, veiculo.getAnoFabricacao());
        assertEquals(clienteId, veiculo.getClienteId());
    }

    @Test
    @DisplayName("reconstituir: rejeita id nulo")
    void reconstituirDeveRejeitarIdNulo() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> Veiculo.reconstituir(null, 2020, "ABC1234", "Civic", "Honda", clienteId));

        assertEquals("O ID do veículo é inválido", exception.getMessage());
    }

    // atualizar

    @Test
    @DisplayName("atualizar: substitui todos os dados normalizando os valores")
    void atualizarDeveSubstituirTodosOsDados() {
        Veiculo veiculo = criarVeiculo();
        UUID id = veiculo.getId();
        UUID novoClienteId = UUID.randomUUID();

        veiculo.atualizar(2022, "xyz1d23", "  Corolla  ", "  Toyota  ", novoClienteId);

        assertEquals(id, veiculo.getId());
        assertEquals(2022, veiculo.getAnoFabricacao());
        assertEquals("XYZ1D23", veiculo.getPlaca());
        assertEquals("Corolla", veiculo.getModelo());
        assertEquals("Toyota", veiculo.getMarca());
        assertEquals(novoClienteId, veiculo.getClienteId());
    }

    @Test
    @DisplayName("atualizar: se qualquer valor for inválido, nada é alterado")
    void atualizarNaoDeveAlterarNadaQuandoAlgumValorForInvalido() {
        Veiculo veiculo = criarVeiculo();
        UUID outro = UUID.randomUUID();

        assertThrows(IllegalArgumentException.class, () -> veiculo.atualizar(2022, "AB123", "Corolla", "Toyota", outro));
        assertThrows(IllegalArgumentException.class, () -> veiculo.atualizar(2022, "XYZ1D23", " ", "Toyota", outro));
        assertThrows(IllegalArgumentException.class, () -> veiculo.atualizar(2022, "XYZ1D23", "Corolla", " ", outro));
        assertThrows(IllegalArgumentException.class, () -> veiculo.atualizar(1899, "XYZ1D23", "Corolla", "Toyota", outro));
        assertThrows(IllegalArgumentException.class, () -> veiculo.atualizar(2022, "XYZ1D23", "Corolla", "Toyota", null));

        assertEquals(2020, veiculo.getAnoFabricacao());
        assertEquals("ABC1234", veiculo.getPlaca());
        assertEquals("Civic", veiculo.getModelo());
        assertEquals("Honda", veiculo.getMarca());
        assertEquals(clienteId, veiculo.getClienteId());
    }
}
