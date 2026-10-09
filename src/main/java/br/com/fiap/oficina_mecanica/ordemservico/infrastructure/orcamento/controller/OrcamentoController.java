package br.com.fiap.oficina_mecanica.ordemservico.infrastructure.orcamento.controller;

import br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.dto.AprovacaoClienteInput;
import br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.dto.AprovacaoInput;
import br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.dto.GerarOrcamentoInput;
import br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.dto.OrcamentoOutput;
import br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.usecase.AprovarOrcamentoClienteUseCase;
import br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.usecase.AprovarOrcamentoUseCase;
import br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.usecase.BuscarOrcamentoClienteUseCase;
import br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.usecase.BuscarOrcamentoUseCase;
import br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.usecase.GerarOrcamentoUseCase;
import br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.usecase.RejeitarOrcamentoClienteUseCase;
import br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.usecase.RejeitarOrcamentoUseCase;
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
    private final AprovarOrcamentoClienteUseCase aprovarOrcamentoClienteUseCase;
    private final RejeitarOrcamentoClienteUseCase rejeitarOrcamentoClienteUseCase;
    private final BuscarOrcamentoClienteUseCase buscarOrcamentoClienteUseCase;

    public OrcamentoController(
            GerarOrcamentoUseCase gerarOrcamentoUseCase,
            AprovarOrcamentoUseCase aprovarOrcamentoUseCase,
            RejeitarOrcamentoUseCase rejeitarOrcamentoUseCase,
            BuscarOrcamentoUseCase buscarOrcamentoUseCase,
            BuscarOrcamentoClienteUseCase buscarOrcamentoClienteUseCase,
            AprovarOrcamentoClienteUseCase aprovarOrcamentoClienteUseCase,
            RejeitarOrcamentoClienteUseCase rejeitarOrcamentoClienteUseCase) {
        this.gerarOrcamentoUseCase = gerarOrcamentoUseCase;
        this.aprovarOrcamentoUseCase = aprovarOrcamentoUseCase;
        this.rejeitarOrcamentoUseCase = rejeitarOrcamentoUseCase;
        this.buscarOrcamentoUseCase = buscarOrcamentoUseCase;
        this.buscarOrcamentoClienteUseCase = buscarOrcamentoClienteUseCase;
        this.aprovarOrcamentoClienteUseCase = aprovarOrcamentoClienteUseCase;
        this.rejeitarOrcamentoClienteUseCase = rejeitarOrcamentoClienteUseCase;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    public ResponseEntity<OrcamentoOutput> gerarOrcamento(@RequestBody GerarOrcamentoInput input) {
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

    @PostMapping("/{id}/cliente/visualizar")
    public ResponseEntity<OrcamentoOutput> visualizarPeloCliente(
            @PathVariable UUID id,
            @RequestBody AprovacaoClienteInput input) {
        OrcamentoOutput orcamento = buscarOrcamentoClienteUseCase.execute(id, input.cpf(), input.codigoAcesso());
        return ResponseEntity.ok(orcamento);
    }

    @PostMapping("/{id}/cliente/aprovar")
    public ResponseEntity<OrcamentoOutput> aprovarPeloCliente(
            @PathVariable UUID id,
            @RequestBody AprovacaoClienteInput input) {
        OrcamentoOutput aprovado = aprovarOrcamentoClienteUseCase.execute(id, input);
        return ResponseEntity.ok(aprovado);
    }

    @PostMapping("/{id}/cliente/rejeitar")
    public ResponseEntity<OrcamentoOutput> rejeitarPeloCliente(
            @PathVariable UUID id,
            @RequestBody AprovacaoClienteInput input) {
        OrcamentoOutput rejeitado = rejeitarOrcamentoClienteUseCase.execute(id, input.cpf(), input.codigoAcesso());
        return ResponseEntity.ok(rejeitado);
    }
}