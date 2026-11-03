package br.com.fiap.oficina_mecanica.cliente.application.usecase;

import br.com.fiap.oficina_mecanica.cliente.application.dto.AtualizarClienteInput;
import br.com.fiap.oficina_mecanica.cliente.application.dto.CriarClienteInput;
import br.com.fiap.oficina_mecanica.cliente.application.dto.EnderecoInput;
import br.com.fiap.oficina_mecanica.cliente.domain.Cliente;
import br.com.fiap.oficina_mecanica.cliente.domain.Estado;

final class ClienteFixtures {

    static final String CPF = "529.982.247-25";
    static final String OUTRO_CPF = "000.000.037-00";

    private ClienteFixtures() {
    }

    static EnderecoInput enderecoInput() {
        return new EnderecoInput("rua das flores", "123", null, "centro", "são paulo", Estado.SP, "01001-000");
    }

    static CriarClienteInput criarInput() {
        return criarInput(CPF);
    }

    static CriarClienteInput criarInput(String documento) {
        return new CriarClienteInput("Maria Silva", documento, "(11) 98765-4321", "maria@email.com", enderecoInput());
    }

    static CriarClienteInput criarInput(String nome, String documento, String telefone, String email,
                                        EnderecoInput endereco) {
        return new CriarClienteInput(nome, documento, telefone, email, endereco);
    }

    static AtualizarClienteInput atualizarInput() {
        return new AtualizarClienteInput("João Souza", OUTRO_CPF, "(21) 3456-7890", "Joao@Email.com",
                new EnderecoInput("avenida brasil", "10", "Bloco B", "jardim américa", "rio de janeiro", Estado.RJ,
                        "20000-000"));
    }

    static AtualizarClienteInput atualizarInput(String nome, String documento, String telefone, String email,
                                                EnderecoInput endereco) {
        return new AtualizarClienteInput(nome, documento, telefone, email, endereco);
    }

    static Cliente cliente() {
        return cliente(CPF);
    }

    static Cliente cliente(String documento) {
        return new Cliente(documento, "Maria Silva", "(11) 98765-4321", "maria@email.com",
                EnderecoInput.paraDominio(enderecoInput()));
    }
}
