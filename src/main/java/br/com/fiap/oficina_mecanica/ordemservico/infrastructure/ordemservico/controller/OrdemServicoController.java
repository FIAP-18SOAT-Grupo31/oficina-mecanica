package br.com.fiap.oficina_mecanica.ordemservico.infrastructure.ordemservico.controller;

import br.com.fiap.oficina_mecanica.compartilhado.domain.Pagina;
import br.com.fiap.oficina_mecanica.compartilhado.domain.Paginacao;
import br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.dto.CriarOrdemServicoInput;
import br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.dto.CriarOrdemServicoOutput;
import br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.dto.OrdemServicoDetalheOutput;
import br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.dto.OrdemServicoResumoOutput;
import br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.usecase.BuscarOrdemServicoUseCase;
import br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.usecase.CancelarOrdemServicoUseCase;
import br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.usecase.CriarOrdemServicoUseCase;
import br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.usecase.EntregarOrdemServicoUseCase;
import br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.usecase.FinalizarOrdemServicoUseCase;
import br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.usecase.IniciarExecucaoOrdemServicoUseCase;
import br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.usecase.ListarOrdensServicoUseCase;
import br.com.fiap.oficina_mecanica.ordemservico.domain.ordemservico.StatusOrdemServico;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/ordens-servico")
public class OrdemServicoController {

    private final CriarOrdemServicoUseCase criarOrdemServicoUseCase;

    private final ListarOrdensServicoUseCase listarOrdensServicoUseCase;
    private final BuscarOrdemServicoUseCase buscarOrdemServicoUseCase;
    private final CancelarOrdemServicoUseCase cancelarOrdemServicoUseCase;
    private final IniciarExecucaoOrdemServicoUseCase iniciarExecucaoOrdemServicoUseCase;
    private final FinalizarOrdemServicoUseCase finalizarOrdemServicoUseCase;
    private final EntregarOrdemServicoUseCase entregarOrdemServicoUseCase;

    public OrdemServicoController(CriarOrdemServicoUseCase criarOrdemServicoUseCase,
                                  ListarOrdensServicoUseCase listarOrdensServicoUseCase,
                                  BuscarOrdemServicoUseCase buscarOrdemServicoUseCase,
                                  CancelarOrdemServicoUseCase cancelarOrdemServicoUseCase,
                                  IniciarExecucaoOrdemServicoUseCase iniciarExecucaoOrdemServicoUseCase,
                                  FinalizarOrdemServicoUseCase finalizarOrdemServicoUseCase,
                                  EntregarOrdemServicoUseCase entregarOrdemServicoUseCase) {
        this.criarOrdemServicoUseCase = criarOrdemServicoUseCase;
        this.listarOrdensServicoUseCase = listarOrdensServicoUseCase;
        this.buscarOrdemServicoUseCase = buscarOrdemServicoUseCase;
        this.cancelarOrdemServicoUseCase = cancelarOrdemServicoUseCase;
        this.iniciarExecucaoOrdemServicoUseCase = iniciarExecucaoOrdemServicoUseCase;
        this.finalizarOrdemServicoUseCase = finalizarOrdemServicoUseCase;
        this.entregarOrdemServicoUseCase = entregarOrdemServicoUseCase;
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    public ResponseEntity<OrdemServicoDetalheOutput> buscarOrdemServico(@PathVariable UUID id) {
        return ResponseEntity.ok(buscarOrdemServicoUseCase.execute(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    public ResponseEntity<Pagina<OrdemServicoResumoOutput>> listarOrdensServico(
            @RequestParam(required = false) StatusOrdemServico status,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "deve ser maior ou igual a 0") int pagina,
            @RequestParam(defaultValue = "20")
            @Min(value = 1, message = "deve ser entre 1 e 100")
            @Max(value = Paginacao.TAMANHO_MAXIMO, message = "deve ser entre 1 e 100") int tamanho) {
        return ResponseEntity.ok(listarOrdensServicoUseCase.execute(status, new Paginacao(pagina, tamanho)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    public ResponseEntity<CriarOrdemServicoOutput> criarOrdemServico(@Valid @RequestBody CriarOrdemServicoInput input,
                                                                     UriComponentsBuilder uriBuilder) {
        CriarOrdemServicoOutput ordemServicoCriada = criarOrdemServicoUseCase.execute(input);
        URI location = uriBuilder.path("/api/ordens-servico/{id}")
                .buildAndExpand(ordemServicoCriada.ordemServicoId())
                .toUri();
        return ResponseEntity.created(location).body(ordemServicoCriada);
    }

    @PatchMapping("/{id}/cancelar")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    public ResponseEntity<Void> cancelarOrdemServico(@PathVariable UUID id) {
        cancelarOrdemServicoUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/iniciar-execucao")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    public ResponseEntity<Void> iniciarExecucao(@PathVariable UUID id) {
        iniciarExecucaoOrdemServicoUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/finalizar")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    public ResponseEntity<Void> finalizarOrdemServico(@PathVariable UUID id) {
        finalizarOrdemServicoUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/entregar")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    public ResponseEntity<Void> entregarOrdemServico(@PathVariable UUID id) {
        entregarOrdemServicoUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }
}
