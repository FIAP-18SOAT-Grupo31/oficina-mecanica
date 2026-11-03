package br.com.fiap.oficina_mecanica.cliente.domain;

public class Endereco {
    private final String rua;
    private final String numero;
    private final String complemento;
    private final String bairro;
    private final String cidade;
    private final Estado estado;
    private final String cep;

    public Endereco(String rua, String numero, String complemento, String bairro, String cidade, Estado estado, String cep) {
        String ruaNormalizada = validaString(rua, "rua");
        String numeroNormalizado = validaString(numero, "número");
        String bairroNormalizado = validaString(bairro, "bairro");
        String cidadeNormalizada = validaString(cidade, "cidade");
        String cepNormalizado = validaCep(cep);
        String complementoNormalizado = validaComplemento(complemento);
        validaEstado(estado);


        this.rua = deixaPrimeiraLetraMaiuscula(ruaNormalizada);
        this.numero = numeroNormalizado;
        this.complemento = complementoNormalizado;
        this.bairro =  deixaPrimeiraLetraMaiuscula(bairroNormalizado);
        this.cidade = deixaPrimeiraLetraMaiuscula(cidadeNormalizada);
        this.estado = estado;
        this.cep = cepNormalizado;
    }

    private void validaEstado(Estado estado) {
        if(estado == null ){
            throw new IllegalArgumentException("O Estado do endereço é inválido");
        }
    }

    private String validaCep(String cep) {
        String cepNormalizado = validaString(cep, "cep").replaceAll("\\D", "");

        if(cepNormalizado.length() != 8 || cep.matches(".*[a-zA-Z].*")){
            throw new IllegalArgumentException("O CEP do endereço é inválido");
        }

        return cepNormalizado;
    }

    private String validaComplemento(String complemento) {
        String complementoNormalizado = null;
        if(complemento != null && !complemento.isBlank()){
           complementoNormalizado = complemento.trim();
       }
        return complementoNormalizado;
    }

    private String validaString(String string, String nomeAtributo) {
        if (string == null || string.isBlank()) {
            throw new IllegalArgumentException(String.format("O(a) %s do endereço é inválido(a)", nomeAtributo));
        }

        return string.trim();
    }

    private String deixaPrimeiraLetraMaiuscula(String texto) {
        String[] palavras = texto.split("\\s");
        StringBuilder resultado = new StringBuilder();

        for (int i = 0; i < palavras.length; i++) {
            if (!palavras[i].isEmpty()) {
                resultado.append(Character.toUpperCase(palavras[i].charAt(0)))
                        .append(palavras[i].substring(1).toLowerCase());

                if (i < palavras.length - 1) {
                    resultado.append(" ");
                }
            }
        }

        return resultado.toString();
    }

    public String converterParaString() {
        return String.format("Rua: %s, Número: %s, Complemento: %s, Bairro: %s, Cidade: %s, Estado: %s, CEP: %s",
                this.rua,
                this.numero,
                (this.complemento != null ? this.complemento : ""),
                this.bairro,
                this.cidade,
                this.estado,
                this.cep);
    }


    public Estado getEstado() {
        return estado;
    }

    public String getBairro() {
        return bairro;
    }

    public String getCidade() {
        return cidade;
    }

    public String getComplemento() {
        return complemento;
    }

    public String getNumero() {
        return numero;
    }

    public String getRua() {
        return rua;
    }

    public String getCep() {
        return cep;
    }
}
