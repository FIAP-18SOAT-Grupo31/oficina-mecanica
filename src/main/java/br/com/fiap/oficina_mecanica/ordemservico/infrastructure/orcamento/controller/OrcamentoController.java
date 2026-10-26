package br.com.fiap.oficina_mecanica.ordemservico.infrastructure.orcamento.controller;

import br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.dto.AprovacaoInput;
import br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.dto.GerarOrcamentoInput;
import br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.dto.OrcamentoOutput;
import br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.usecase.AprovarOrcamentoUseCase;
import br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.usecase.BuscarOrcamentoUseCase;
import br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.usecase.GerarOrcamentoUseCase;
import br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.usecase.RejeitarOrcamentoUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/orcamentos")
public class OrcamentoController {

    private final GerarOrcamentoUseCase gerarOrcamentoUseCase;
    private final AprovarOrcamentoUseCase aprovarOrcamentoUseCase;
    private final RejeitarOrcamentoUseCase rejeitarOrcamentoUseCase;
    private final BuscarOrcamentoUseCase buscarOrcamentoUseCase;

    public OrcamentoController(
            GerarOrcamentoUseCase gerarOrcamentoUseCase,
            AprovarOrcamentoUseCase aprovarOrcamentoUseCase,
            RejeitarOrcamentoUseCase rejeitarOrcamentoUseCase,
            BuscarOrcamentoUseCase buscarOrcamentoUseCase) {
        this.gerarOrcamentoUseCase = gerarOrcamentoUseCase;
        this.aprovarOrcamentoUseCase = aprovarOrcamentoUseCase;
        this.rejeitarOrcamentoUseCase = rejeitarOrcamentoUseCase;
        this.buscarOrcamentoUseCase = buscarOrcamentoUseCase;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    public ResponseEntity<OrcamentoOutput> gerarOrcamento(@Valid @RequestBody GerarOrcamentoInput input) {
        OrcamentoOutput orcamentoGerado = gerarOrcamentoUseCase.execute(input);
        return ResponseEntity.status(HttpStatus.CREATED).body(orcamentoGerado);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    public ResponseEntity<OrcamentoOutput> buscarOrcamento(@PathVariable UUID id) {
        OrcamentoOutput orcamento = buscarOrcamentoUseCase.execute(id);
        return ResponseEntity.ok(orcamento);
    }

    @PatchMapping("/{id}/aprovar")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    public ResponseEntity<OrcamentoOutput> aprovarOrcamento(
            @PathVariable UUID id,
            @RequestBody(required = false) AprovacaoInput input) {
        OrcamentoOutput orcamentoAprovado = aprovarOrcamentoUseCase.execute(id, input);
        return ResponseEntity.ok(orcamentoAprovado);
    }

    @PatchMapping("/{id}/rejeitar")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    public ResponseEntity<OrcamentoOutput> rejeitarOrcamento(@PathVariable UUID id) {
        OrcamentoOutput orcamentoRejeitado = rejeitarOrcamentoUseCase.execute(id);
        return ResponseEntity.ok(orcamentoRejeitado);
    }
}
