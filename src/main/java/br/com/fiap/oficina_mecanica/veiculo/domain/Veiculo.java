package br.com.fiap.oficina_mecanica.veiculo.domain;

import java.time.Year;
import java.util.UUID;

public class Veiculo {
    private final UUID id;
    private int anoFabricacao;
    private String placa;
    private String modelo;
    private String marca;
    private UUID clienteId;

    public Veiculo(int anoFabricacao, String placa, String modelo, String marca, UUID clienteId) {
        this(UUID.randomUUID(), anoFabricacao, placa, modelo, marca, clienteId);
    }

    private Veiculo(UUID id, int anoFabricacao, String placa, String modelo, String marca, UUID clienteId) {
        if (id == null) {
            throw new IllegalArgumentException("O ID do veículo é inválido");
        }

        String placaNormalizada = validaPlaca(placa);
        String modeloNormalizado = validaString(modelo, "modelo");
        String marcaNormalizada = validaString(marca, "marca");
        validaAnoFabricacao(anoFabricacao);
        validaClienteId(clienteId);

        this.id = id;
        this.anoFabricacao = anoFabricacao;
        this.clienteId = clienteId;
        this.placa = placaNormalizada;
        this.marca = marcaNormalizada;
        this.modelo = modeloNormalizado;
    }

    public static Veiculo reconstituir(UUID id, int anoFabricacao, String placa, String modelo, String marca,
                                       UUID clienteId) {
        return new Veiculo(id, anoFabricacao, placa, modelo, marca, clienteId);
    }

    /**
     * Atualiza todos os dados do veículo de uma vez: se algum valor for inválido,
     * nada é alterado.
     */
    public void atualizar(int anoFabricacao, String placa, String modelo, String marca, UUID clienteId) {
        String placaNormalizada = validaPlaca(placa);
        String modeloNormalizado = validaString(modelo, "modelo");
        String marcaNormalizada = validaString(marca, "marca");
        validaAnoFabricacao(anoFabricacao);
        validaClienteId(clienteId);

        this.anoFabricacao = anoFabricacao;
        this.clienteId = clienteId;
        this.placa = placaNormalizada;
        this.marca = marcaNormalizada;
        this.modelo = modeloNormalizado;
    }

    private static String validaPlaca(String placa) {
        String placaNormalizada = validaString(placa, "placa").replaceAll("[^a-zA-Z0-9]", "").toUpperCase();

        if (!placa.matches("^[a-zA-Z0-9-]+$")) {
            throw new IllegalArgumentException("A placa do veículo é inválida.");
        }

        // "^[A-Z]{3}[0-9][A-Z][0-9]{2}$" -> padrão mercosul
        // "^[A-Z]{3}-?[0-9]{4}$" -> padrão antigo
        if (placaNormalizada.matches("^[A-Z]{3}[0-9][A-Z][0-9]{2}$") || placaNormalizada.matches("^[A-Z]{3}-?[0-9]{4}$")) {
            return placaNormalizada;
        }
        throw new IllegalArgumentException("A placa do veículo é inválida.");
    }

    private static void validaAnoFabricacao(int anoFabricacao) {
        if (anoFabricacao < 1900 || anoFabricacao > Year.now().getValue()) {
            throw new IllegalArgumentException("Ano de fabricação do veículo é inválido.");
        }
    }

    private static void validaClienteId(UUID clienteId) {
        if (clienteId == null) {
            throw new IllegalArgumentException("O cliente do veículo é inválido.");
        }
    }

    private static String validaString(String string, String nomeAtributo) {
        if (string == null || string.isBlank()) {
            throw new IllegalArgumentException(String.format("O(a) %s do veículo é inválido(a)", nomeAtributo));
        }

        return string.trim();
    }

    public UUID getId() {
        return id;
    }

    public int getAnoFabricacao() {
        return anoFabricacao;
    }

    public UUID getClienteId() {
        return clienteId;
    }

    public String getMarca() {
        return marca;
    }

    public String getPlaca() {
        return placa;
    }

    public String getModelo() {
        return modelo;
    }

    public void setAnoFabricacao(int anoFabricacao) {
        validaAnoFabricacao(anoFabricacao);
        this.anoFabricacao = anoFabricacao;
    }

    public void setPlaca(String placa) {
        this.placa = validaPlaca(placa);
    }

    public void setModelo(String modelo) {
        this.modelo = validaString(modelo, "modelo");
    }

    public void setMarca(String marca) {
        this.marca = validaString(marca, "marca");
    }

    public void setClienteId(UUID clienteId) {
        validaClienteId(clienteId);
        this.clienteId = clienteId;
    }
}
