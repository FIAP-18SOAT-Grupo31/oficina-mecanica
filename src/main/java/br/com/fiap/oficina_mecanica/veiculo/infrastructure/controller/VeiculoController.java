package br.com.fiap.oficina_mecanica.veiculo.infrastructure.controller;

import br.com.fiap.oficina_mecanica.compartilhado.application.dto.PaginaOutput;
import br.com.fiap.oficina_mecanica.veiculo.application.dto.AtualizarVeiculoInput;
import br.com.fiap.oficina_mecanica.veiculo.application.dto.CriarVeiculoInput;
import br.com.fiap.oficina_mecanica.veiculo.application.dto.ListarVeiculosInput;
import br.com.fiap.oficina_mecanica.veiculo.application.dto.VeiculoOutput;
import br.com.fiap.oficina_mecanica.veiculo.application.usecase.AtualizarVeiculoUseCase;
import br.com.fiap.oficina_mecanica.veiculo.application.usecase.BuscarVeiculoUseCase;
import br.com.fiap.oficina_mecanica.veiculo.application.usecase.CriarVeiculoUseCase;
import br.com.fiap.oficina_mecanica.veiculo.application.usecase.ExcluirVeiculoUseCase;
import br.com.fiap.oficina_mecanica.veiculo.application.usecase.ListarVeiculosUseCase;
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
@RequestMapping("/api/veiculos")
@Tag(name = "Veículos")
public class VeiculoController {

    private final CriarVeiculoUseCase criarVeiculoUseCase;
    private final BuscarVeiculoUseCase buscarVeiculoUseCase;
    private final ListarVeiculosUseCase listarVeiculosUseCase;
    private final AtualizarVeiculoUseCase atualizarVeiculoUseCase;
    private final ExcluirVeiculoUseCase excluirVeiculoUseCase;

    public VeiculoController(CriarVeiculoUseCase criarVeiculoUseCase,
                             BuscarVeiculoUseCase buscarVeiculoUseCase,
                             ListarVeiculosUseCase listarVeiculosUseCase,
                             AtualizarVeiculoUseCase atualizarVeiculoUseCase,
                             ExcluirVeiculoUseCase excluirVeiculoUseCase) {
        this.criarVeiculoUseCase = criarVeiculoUseCase;
        this.buscarVeiculoUseCase = buscarVeiculoUseCase;
        this.listarVeiculosUseCase = listarVeiculosUseCase;
        this.atualizarVeiculoUseCase = atualizarVeiculoUseCase;
        this.excluirVeiculoUseCase = excluirVeiculoUseCase;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @Operation(summary = "Cadastra um veículo para um cliente")
    public ResponseEntity<VeiculoOutput> criar(@Valid @RequestBody CriarVeiculoInput input,
                                               UriComponentsBuilder uriBuilder) {
        VeiculoOutput veiculoCriado = criarVeiculoUseCase.execute(input);
        URI location = uriBuilder.path("/api/veiculos/{id}")
                .buildAndExpand(veiculoCriado.id())
                .toUri();
        return ResponseEntity.created(location).body(veiculoCriado);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @Operation(summary = "Busca um veículo pelo id")
    public ResponseEntity<VeiculoOutput> buscar(@PathVariable UUID id) {
        return ResponseEntity.ok(buscarVeiculoUseCase.execute(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @Operation(summary = "Lista os veículos de forma paginada, com filtro opcional por cliente, placa e modelo")
    public ResponseEntity<PaginaOutput<VeiculoOutput>> listar(
            @RequestParam(required = false) UUID clienteId,
            @RequestParam(required = false) String placa,
            @RequestParam(required = false) String modelo,
            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "deve ser maior ou igual a 0") int pagina,
            @RequestParam(defaultValue = "20")
            @Min(value = 1, message = "deve ser maior ou igual a 1")
            @Max(value = 100, message = "deve ser menor ou igual a 100") int tamanho) {
        return ResponseEntity.ok(
                listarVeiculosUseCase.execute(new ListarVeiculosInput(clienteId, placa, modelo, pagina, tamanho)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @Operation(summary = "Atualiza todos os dados de um veículo, inclusive o cliente dono")
    public ResponseEntity<VeiculoOutput> atualizar(@PathVariable UUID id,
                                                   @Valid @RequestBody AtualizarVeiculoInput input) {
        return ResponseEntity.ok(atualizarVeiculoUseCase.execute(id, input));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @Operation(summary = "Exclui um veículo")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        excluirVeiculoUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }
}
