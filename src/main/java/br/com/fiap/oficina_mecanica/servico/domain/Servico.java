package br.com.fiap.oficina_mecanica.servico.domain;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Serviço do catálogo da oficina (ex.: troca de óleo, alinhamento). O orçamento copia o nome e o valor da mão de obra
 * no momento em que é gerado, então alterar o catálogo não muda orçamentos antigos.
 */
public class Servico {

    private static final int TAMANHO_MAXIMO_NOME = 100;
    private static final int TAMANHO_MAXIMO_DESCRICAO = 255;

    private final UUID id;
    private String nome;
    private String descricao;
    private BigDecimal valorMaoDeObra;

    public Servico(String nome, String descricao, BigDecimal valorMaoDeObra) {
        this(UUID.randomUUID(), nome, descricao, valorMaoDeObra);
    }

    private Servico(UUID id, String nome, String descricao, BigDecimal valorMaoDeObra) {
        if (id == null) {
            throw new IllegalArgumentException("O ID do serviço é inválido");
        }
        this.id = id;
        atualizar(nome, descricao, valorMaoDeObra);
    }

    public static Servico reconstituir(UUID id, String nome, String descricao, BigDecimal valorMaoDeObra) {
        return new Servico(id, nome, descricao, valorMaoDeObra);
    }

    public void atualizar(String nome, String descricao, BigDecimal valorMaoDeObra) {
        String nomeValidado = validaNome(nome);
        String descricaoValidada = validaDescricao(descricao);
        BigDecimal valorValidado = validaValor(valorMaoDeObra);

        this.nome = nomeValidado;
        this.descricao = descricaoValidada;
        this.valorMaoDeObra = valorValidado;
    }

    private static String validaNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome do serviço é obrigatório");
        }
        String nomeNormalizado = nome.trim().replaceAll("\\s+", " ");
        if (nomeNormalizado.length() > TAMANHO_MAXIMO_NOME) {
            throw new IllegalArgumentException("O nome do serviço deve ter no máximo 100 caracteres");
        }
        return nomeNormalizado;
    }

    private static String validaDescricao(String descricao) {
        if (descricao == null || descricao.isBlank()) {
            return null;
        }
        String descricaoNormalizada = descricao.trim();
        if (descricaoNormalizada.length() > TAMANHO_MAXIMO_DESCRICAO) {
            throw new IllegalArgumentException("A descrição do serviço deve ter no máximo 255 caracteres");
        }
        return descricaoNormalizada;
    }

    private static BigDecimal validaValor(BigDecimal valorMaoDeObra) {
        if (valorMaoDeObra == null || valorMaoDeObra.signum() <= 0) {
            throw new IllegalArgumentException("O valor da mão de obra deve ser maior que zero");
        }
        if (valorMaoDeObra.stripTrailingZeros().scale() > 2) {
            throw new IllegalArgumentException("O valor da mão de obra deve ter no máximo 2 casas decimais");
        }
        return valorMaoDeObra;
    }

    public UUID getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public BigDecimal getValorMaoDeObra() {
        return valorMaoDeObra;
    }
}
