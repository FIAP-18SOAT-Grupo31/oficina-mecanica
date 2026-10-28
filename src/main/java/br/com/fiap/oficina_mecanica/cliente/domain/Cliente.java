package br.com.fiap.oficina_mecanica.cliente.domain;

import java.util.UUID;

public class Cliente {

    private UUID id;
    private String documento;
    private String nome;
    private String telefone;
    private String email;
    private Endereco endereco;

    public Cliente(String documento, String nome, String telefone,
                   String email, Endereco endereco) {
        this(
                UUID.randomUUID(),
                documento,
                nome,
                telefone,
                email,
                endereco
        );
    }

    private Cliente(UUID id, String documento, String nome,
                    String telefone, String email, Endereco endereco) {
        if (id == null) {
            throw new IllegalArgumentException("O ID do cliente é inválido");
        }

        String documentoNormalizado = validaDocumento(documento);
        String telefoneNormalizado = validaTelefone(telefone);
        String emailNormalizado = validaEmail(email);
        String nomeNormalizado = validaString(nome, "nome");
        validaEndereco(endereco);

        this.id = id;
        this.documento = documentoNormalizado;
        this.nome = nomeNormalizado;
        this.telefone = telefoneNormalizado;
        this.email = emailNormalizado;
        this.endereco = endereco;
    }

    public static Cliente reconstituir(UUID id, String documento,
                                       String nome, String telefone,
                                       String email, Endereco endereco) {
        return new Cliente(id, documento, nome, telefone, email, endereco);
    }

    private static String validaDocumento(String documento) {
        String documentoNormalizado = validaString(documento, "documento").replaceAll("\\W", "").toUpperCase();

        if (documentoNormalizado.length() == 11 && validaCPF(documentoNormalizado)) {
            return documentoNormalizado;
        }

        if (documentoNormalizado.length() == 14 && validaCNPJ(documentoNormalizado)) {
            return documentoNormalizado;
        }

        throw new IllegalArgumentException("O documento do cliente é inválido");
    }

    private static String validaTelefone(String telefone) {
        String telefoneNormalizado = validaString(telefone, "telefone").replaceAll("\\D", "");

        if (telefoneNormalizado.matches("^\\d{10,11}$")) {
            return telefoneNormalizado;
        }

        throw new IllegalArgumentException("O telefone do cliente é inválido");
    }

    private static void validaEndereco(Endereco endereco) {
        if (endereco == null) {
            throw new IllegalArgumentException("O endereço do cliente é inválido");
        }
    }

    private static String validaEmail(String email) {
        if (email == null) {
            throw new IllegalArgumentException("O e-mail do cliente é inválido");
        }

        String emailNormalizado = email.trim().toLowerCase();

        if (emailNormalizado.length() > 254) {
            throw new IllegalArgumentException("O e-mail do cliente é inválido");
        }

        // Precisa ter um único "@" e a parte local não pode ser vazia
        int arroba = emailNormalizado.indexOf('@');
        if (arroba < 1 || arroba != emailNormalizado.lastIndexOf('@')) {
            throw new IllegalArgumentException("O e-mail do cliente é inválido");
        }

        String local = emailNormalizado.substring(0, arroba);
        String dominio = emailNormalizado.substring(arroba + 1);

        if (local.length() > 64
                || !local.matches("[a-z0-9!#$%&'*+/=?^_`{|}~-]+(\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*")
                || !dominio.matches("([a-z0-9]([a-z0-9-]{0,61}[a-z0-9])?\\.)+[a-z]{2,63}")) {
            throw new IllegalArgumentException("O e-mail do cliente é inválido");
        }

        return emailNormalizado;
    }

    private static String validaString(String string, String nomeAtributo) {
        if (string == null || string.isBlank()) {
            throw new IllegalArgumentException(String.format("O(a) %s do cliente é inválido(a).", nomeAtributo));
        }

        return string.trim();
    }

    private static boolean validaCPF(String cpf) {

        String cpfNormalizado = cpf.replaceAll("\\D", "");

        // Validações básicas de sequências repetidas (ex: 111.111.111-11)
        if (cpfNormalizado.matches("(\\d)\\1{10}")) return false;


        // Cálculo do 1º dígito verificador
        int soma = 0, peso = 10;
        for (int i = 0; i < 9; i++) {
            soma += (cpfNormalizado.charAt(i) - '0') * peso--;
        }
        int digito1 = 11 - (soma % 11);
        if (digito1 > 9) digito1 = 0;

        // Cálculo do 2º dígito verificador
        soma = 0;
        peso = 11;
        for (int i = 0; i < 10; i++) {
            soma += (cpfNormalizado.charAt(i) - '0') * peso--;
        }
        int digito2 = 11 - (soma % 11);
        if (digito2 > 9) digito2 = 0;

        return (digito1 == (cpfNormalizado.charAt(9) - '0')) && (digito2 == (cpfNormalizado.charAt(10) - '0'));

    }


    // Valida um CNPJ numérico ou alfanumérico.
    private static boolean validaCNPJ(String cnpjNormalizado) {

        // 12 caracteres alfanuméricos + 2 dígitos verificadores numéricos
        if (!cnpjNormalizado.matches("[0-9A-Z]{12}[0-9]{2}")) {
            return false;
        }

        // Rejeita sequências com todos os caracteres iguais (ex.: 00000000000000)
        if (cnpjNormalizado.chars().distinct().count() == 1) {
            return false;
        }

        // Calcula e confere o 1º DV (posição 12) e o 2º DV (posição 13).
        // Módulo 11, pesos de 2 a 9 da direita para a esquerda (reiniciando após o 9);
        // valor do caractere = código ASCII - 48.
        for (int posDv = 12; posDv <= 13; posDv++) {
            int soma = 0;
            int peso = 2;

            for (int i = posDv - 1; i >= 0; i--) {
                soma += (cnpjNormalizado.charAt(i) - 48) * peso;
                peso = (peso == 9) ? 2 : peso + 1;
            }

            int resto = soma % 11;
            int digito = (resto < 2) ? 0 : 11 - resto;

            if (cnpjNormalizado.charAt(posDv) - '0' != digito) {
                return false;
            }
        }

        return true;
    }

    /**
     * Atualiza todos os dados do cliente de uma vez: se algum valor for inválido,
     * nada é alterado.
     */
    public void atualizar(String documento, String nome, String telefone,
                          String email, Endereco endereco) {
        String documentoNormalizado = validaDocumento(documento);
        String telefoneNormalizado = validaTelefone(telefone);
        String emailNormalizado = validaEmail(email);
        String nomeNormalizado = validaString(nome, "nome");
        validaEndereco(endereco);

        this.documento = documentoNormalizado;
        this.nome = nomeNormalizado;
        this.telefone = telefoneNormalizado;
        this.email = emailNormalizado;
        this.endereco = endereco;
    }

    public UUID getId() {
        return id;
    }

    public Endereco getEndereco() {
        return endereco;
    }

    public String getDocumento() {
        return documento;
    }

    public String getEmail() {
        return email;
    }

    public String getNome() {
        return nome;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setEmail(String email) {
        this.email = validaEmail(email);
    }

    public void setEndereco(Endereco endereco) {
        validaEndereco(endereco);
        this.endereco = endereco;
    }

    public void setTelefone(String telefone) {
        this.telefone = validaTelefone(telefone);
    }

    public void setNome(String nome) {
        this.nome = validaString(nome, "nome");
    }

    public void setDocumento(String documento) {
        this.documento = validaDocumento(documento);
    }
}
