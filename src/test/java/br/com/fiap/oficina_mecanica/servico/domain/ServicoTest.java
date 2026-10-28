package br.com.fiap.oficina_mecanica.servico.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ServicoTest {

    @Test
    void criaServicoNormalizandoNomeEDescricao() {
        Servico servico = new Servico("  Troca   de óleo ", "  Óleo e filtro  ", new BigDecimal("150.00"));

        assertThat(servico.getId()).isNotNull();
        assertThat(servico.getNome()).isEqualTo("Troca de óleo");
        assertThat(servico.getDescricao()).isEqualTo("Óleo e filtro");
        assertThat(servico.getValorMaoDeObra()).isEqualByComparingTo("150.00");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = "   ")
    void descricaoVaziaViraNula(String descricao) {
        assertThat(new Servico("Alinhamento", descricao, BigDecimal.TEN).getDescricao()).isNull();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = "   ")
    void nomeEhObrigatorio(String nome) {
        assertThatThrownBy(() -> new Servico(nome, null, BigDecimal.TEN))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("O nome do serviço é obrigatório");
    }

    @Test
    void nomeTemNoMaximo100Caracteres() {
        assertThat(new Servico("a".repeat(100), null, BigDecimal.TEN).getNome()).hasSize(100);
        assertThatThrownBy(() -> new Servico("a".repeat(101), null, BigDecimal.TEN))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("O nome do serviço deve ter no máximo 100 caracteres");
    }

    @Test
    void descricaoTemNoMaximo255Caracteres() {
        assertThatThrownBy(() -> new Servico("Alinhamento", "a".repeat(256), BigDecimal.TEN))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A descrição do serviço deve ter no máximo 255 caracteres");
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "-0.01"})
    void valorDaMaoDeObraDeveSerPositivo(String valor) {
        assertThatThrownBy(() -> new Servico("Alinhamento", null, new BigDecimal(valor)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("O valor da mão de obra deve ser maior que zero");
    }

    @Test
    void valorDaMaoDeObraEhObrigatorio() {
        assertThatThrownBy(() -> new Servico("Alinhamento", null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("O valor da mão de obra deve ser maior que zero");
    }

    @Test
    void valorDaMaoDeObraTemNoMaximoDuasCasasDecimais() {
        assertThat(new Servico("Alinhamento", null, new BigDecimal("80.500")).getValorMaoDeObra())
                .isEqualByComparingTo("80.50");
        assertThatThrownBy(() -> new Servico("Alinhamento", null, new BigDecimal("80.555")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("O valor da mão de obra deve ter no máximo 2 casas decimais");
    }

    @Test
    void atualizaOsDadosMantendoOId() {
        Servico servico = new Servico("Alinhamento", null, BigDecimal.TEN);
        UUID id = servico.getId();

        servico.atualizar("Alinhamento e balanceamento", "Quatro rodas", new BigDecimal("120.00"));

        assertThat(servico.getId()).isEqualTo(id);
        assertThat(servico.getNome()).isEqualTo("Alinhamento e balanceamento");
        assertThat(servico.getDescricao()).isEqualTo("Quatro rodas");
        assertThat(servico.getValorMaoDeObra()).isEqualByComparingTo("120.00");
    }

    @Test
    void atualizacaoInvalidaNaoAlteraNada() {
        Servico servico = new Servico("Alinhamento", "Quatro rodas", BigDecimal.TEN);

        assertThatThrownBy(() -> servico.atualizar("Novo nome", null, BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class);

        assertThat(servico.getNome()).isEqualTo("Alinhamento");
        assertThat(servico.getDescricao()).isEqualTo("Quatro rodas");
        assertThat(servico.getValorMaoDeObra()).isEqualByComparingTo("10");
    }

    @Test
    void idEhObrigatorioAoReconstituir() {
        assertThatThrownBy(() -> Servico.reconstituir(null, "Alinhamento", null, BigDecimal.TEN))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("O ID do serviço é inválido");
    }
}
