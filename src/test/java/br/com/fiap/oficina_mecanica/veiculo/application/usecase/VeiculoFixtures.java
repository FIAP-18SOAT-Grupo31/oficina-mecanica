package br.com.fiap.oficina_mecanica.veiculo.application.usecase;

import br.com.fiap.oficina_mecanica.veiculo.application.dto.AtualizarVeiculoInput;
import br.com.fiap.oficina_mecanica.veiculo.application.dto.CriarVeiculoInput;
import br.com.fiap.oficina_mecanica.veiculo.domain.Veiculo;

import java.util.UUID;

final class VeiculoFixtures {

    static final UUID CLIENTE_ID = UUID.randomUUID();

    private VeiculoFixtures() {
    }

    static CriarVeiculoInput criarInput() {
        return new CriarVeiculoInput(CLIENTE_ID, "abc-1d23", "  Fiat ", " Argo ", 2022);
    }

    static CriarVeiculoInput criarInput(UUID clienteId, String placa, String marca, String modelo, Integer ano) {
        return new CriarVeiculoInput(clienteId, placa, marca, modelo, ano);
    }

    static AtualizarVeiculoInput atualizarInput(UUID clienteId, String placa, String marca, String modelo,
                                                Integer ano) {
        return new AtualizarVeiculoInput(clienteId, placa, marca, modelo, ano);
    }

    static Veiculo veiculo() {
        return veiculo("ABC1D23");
    }

    static Veiculo veiculo(String placa) {
        return new Veiculo(2022, placa, "Argo", "Fiat", CLIENTE_ID);
    }
}
