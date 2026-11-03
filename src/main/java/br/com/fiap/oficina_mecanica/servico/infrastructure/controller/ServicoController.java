package br.com.fiap.oficina_mecanica.servico.infrastructure.controller;

import br.com.fiap.oficina_mecanica.compartilhado.application.dto.PaginaOutput;
import br.com.fiap.oficina_mecanica.servico.application.dto.AtualizarServicoInput;
import br.com.fiap.oficina_mecanica.servico.application.dto.CriarServicoInput;
import br.com.fiap.oficina_mecanica.servico.application.dto.ListarServicosInput;
import br.com.fiap.oficina_mecanica.servico.application.dto.ServicoOutput;
import br.com.fiap.oficina_mecanica.servico.application.usecase.AtualizarServicoUseCase;
import br.com.fiap.oficina_mecanica.servico.application.usecase.BuscarServicoUseCase;
import br.com.fiap.oficina_mecanica.servico.application.usecase.CriarServicoUseCase;
import br.com.fiap.oficina_mecanica.servico.application.usecase.ExcluirServicoUseCase;
import br.com.fiap.oficina_mecanica.servico.application.usecase.ListarServicosUseCase;
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
@RequestMapping("/api/servicos")
@Tag(name = "Serviços")
public class ServicoController {

    private final CriarServicoUseCase criarServicoUseCase;
    private final BuscarServicoUseCase buscarServicoUseCase;
    private final ListarServicosUseCase listarServicosUseCase;
    private final AtualizarServicoUseCase atualizarServicoUseCase;
    private final ExcluirServicoUseCase excluirServicoUseCase;

    public ServicoController(CriarServicoUseCase criarServicoUseCase,
                             BuscarServicoUseCase buscarServicoUseCase,
                             ListarServicosUseCase listarServicosUseCase,
                             AtualizarServicoUseCase atualizarServicoUseCase,
                             ExcluirServicoUseCase excluirServicoUseCase) {
        this.criarServicoUseCase = criarServicoUseCase;
        this.buscarServicoUseCase = buscarServicoUseCase;
        this.listarServicosUseCase = listarServicosUseCase;
        this.atualizarServicoUseCase = atualizarServicoUseCase;
        this.excluirServicoUseCase = excluirServicoUseCase;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @Operation(summary = "Cadastra um serviço no catálogo")
    public ResponseEntity<ServicoOutput> criar(@Valid @RequestBody CriarServicoInput input,
                                               UriComponentsBuilder uriBuilder) {
        ServicoOutput servicoCriado = criarServicoUseCase.execute(input);
        URI location = uriBuilder.path("/api/servicos/{id}")
                .buildAndExpand(servicoCriado.id())
                .toUri();
        return ResponseEntity.created(location).body(servicoCriado);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @Operation(summary = "Busca um serviço do catálogo pelo id")
    public ResponseEntity<ServicoOutput> buscar(@PathVariable UUID id) {
        return ResponseEntity.ok(buscarServicoUseCase.execute(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @Operation(summary = "Lista os serviços do catálogo de forma paginada, com filtro opcional por nome")
    public ResponseEntity<PaginaOutput<ServicoOutput>> listar(
            @RequestParam(required = false) String nome,
            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "deve ser maior ou igual a 0") int pagina,
            @RequestParam(defaultValue = "20")
            @Min(value = 1, message = "deve ser maior ou igual a 1")
            @Max(value = 100, message = "deve ser menor ou igual a 100") int tamanho) {
        return ResponseEntity.ok(listarServicosUseCase.execute(new ListarServicosInput(nome, pagina, tamanho)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @Operation(summary = "Atualiza um serviço do catálogo (orçamentos já gerados mantêm o valor antigo)")
    public ResponseEntity<ServicoOutput> atualizar(@PathVariable UUID id,
                                                   @Valid @RequestBody AtualizarServicoInput input) {
        return ResponseEntity.ok(atualizarServicoUseCase.execute(id, input));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @Operation(summary = "Exclui um serviço que ainda não foi usado em orçamentos")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        excluirServicoUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }
}
