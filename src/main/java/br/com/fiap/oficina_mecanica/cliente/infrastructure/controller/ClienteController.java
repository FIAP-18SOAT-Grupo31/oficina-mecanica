package br.com.fiap.oficina_mecanica.cliente.infrastructure.controller;

import br.com.fiap.oficina_mecanica.cliente.application.dto.AtualizarClienteInput;
import br.com.fiap.oficina_mecanica.cliente.application.dto.ClienteOutput;
import br.com.fiap.oficina_mecanica.cliente.application.dto.CriarClienteInput;
import br.com.fiap.oficina_mecanica.cliente.application.dto.ListarClientesInput;
import br.com.fiap.oficina_mecanica.cliente.application.usecase.AtualizarClienteUseCase;
import br.com.fiap.oficina_mecanica.cliente.application.usecase.BuscarClienteUseCase;
import br.com.fiap.oficina_mecanica.cliente.application.usecase.CriarClienteUseCase;
import br.com.fiap.oficina_mecanica.cliente.application.usecase.ExcluirClienteUseCase;
import br.com.fiap.oficina_mecanica.cliente.application.usecase.ListarClientesUseCase;
import br.com.fiap.oficina_mecanica.compartilhado.application.dto.PaginaOutput;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/clientes")
@Tag(name = "Clientes")
public class ClienteController {

    private final CriarClienteUseCase criarClienteUseCase;
    private final BuscarClienteUseCase buscarClienteUseCase;
    private final ListarClientesUseCase listarClientesUseCase;
    private final AtualizarClienteUseCase atualizarClienteUseCase;
    private final ExcluirClienteUseCase excluirClienteUseCase;

    public ClienteController(CriarClienteUseCase criarClienteUseCase,
                             BuscarClienteUseCase buscarClienteUseCase,
                             ListarClientesUseCase listarClientesUseCase,
                             AtualizarClienteUseCase atualizarClienteUseCase,
                             ExcluirClienteUseCase excluirClienteUseCase) {
        this.criarClienteUseCase = criarClienteUseCase;
        this.buscarClienteUseCase = buscarClienteUseCase;
        this.listarClientesUseCase = listarClientesUseCase;
        this.atualizarClienteUseCase = atualizarClienteUseCase;
        this.excluirClienteUseCase = excluirClienteUseCase;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @Operation(summary = "Cadastra um cliente")
    public ResponseEntity<ClienteOutput> criar(@Valid @RequestBody CriarClienteInput input,
                                               UriComponentsBuilder uriBuilder) {
        ClienteOutput clienteCriado = criarClienteUseCase.execute(input);
        URI location = uriBuilder.path("/api/clientes/{id}")
                .buildAndExpand(clienteCriado.id())
                .toUri();
        return ResponseEntity.created(location).body(clienteCriado);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @Operation(summary = "Busca um cliente pelo id")
    public ResponseEntity<ClienteOutput> buscar(@PathVariable UUID id) {
        return ResponseEntity.ok(buscarClienteUseCase.execute(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @Operation(summary = "Lista os clientes de forma paginada, com filtro opcional por nome e documento")
    public ResponseEntity<PaginaOutput<ClienteOutput>> listar(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String documento,
            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "deve ser maior ou igual a 0") int pagina,
            @RequestParam(defaultValue = "20")
            @Min(value = 1, message = "deve ser maior ou igual a 1")
            @Max(value = 100, message = "deve ser menor ou igual a 100") int tamanho) {
        return ResponseEntity.ok(
                listarClientesUseCase.execute(new ListarClientesInput(nome, documento, pagina, tamanho)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @Operation(summary = "Atualiza todos os dados de um cliente")
    public ResponseEntity<ClienteOutput> atualizar(@PathVariable UUID id,
                                                   @Valid @RequestBody AtualizarClienteInput input) {
        return ResponseEntity.ok(atualizarClienteUseCase.execute(id, input));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @Operation(summary = "Exclui um cliente que não tenha veículos vinculados")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        excluirClienteUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }
}
