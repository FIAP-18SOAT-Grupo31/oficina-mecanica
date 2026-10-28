package br.com.fiap.oficina_mecanica.cliente.application.dto;

import br.com.fiap.oficina_mecanica.cliente.domain.Endereco;
import br.com.fiap.oficina_mecanica.cliente.domain.Estado;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EnderecoInput(
        @NotBlank(message = "é obrigatório")
        @Size(max = 150, message = "deve ter no máximo 150 caracteres") String rua,
        @NotBlank(message = "é obrigatório")
        @Size(max = 10, message = "deve ter no máximo 10 caracteres") String numero,
        @Size(max = 100, message = "deve ter no máximo 100 caracteres") String complemento,
        @NotBlank(message = "é obrigatório")
        @Size(max = 100, message = "deve ter no máximo 100 caracteres") String bairro,
        @NotBlank(message = "é obrigatório")
        @Size(max = 100, message = "deve ter no máximo 100 caracteres") String cidade,
        @NotNull(message = "é obrigatório") Estado estado,
        @NotBlank(message = "é obrigatório")
        @Size(max = 9, message = "deve ter no máximo 9 caracteres") String cep
) {

    public static Endereco paraDominio(EnderecoInput input) {
        if (input == null) {
            return null;
        }
        return new Endereco(
                input.rua(),
                input.numero(),
                input.complemento(),
                input.bairro(),
                input.cidade(),
                input.estado(),
                input.cep()
        );
    }
}
