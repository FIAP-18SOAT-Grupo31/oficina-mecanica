package br.com.fiap.oficina_mecanica.ordemservico.infrastructure.orcamento.controller;

import br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.dto.GerarOrcamentoInput;
import br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.dto.OrcamentoOutput;
import br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.usecase.AprovarOrcamentoUseCase;
import br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.usecase.BuscarOrcamentoUseCase;
import br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.usecase.GerarOrcamentoUseCase;
import br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.usecase.RejeitarOrcamentoUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<OrcamentoOutput> gerarOrcamento(@RequestBody GerarOrcamentoInput input) {
        OrcamentoOutput orcamentoGerado = gerarOrcamentoUseCase.execute(input);
        return ResponseEntity.status(HttpStatus.CREATED).body(orcamentoGerado);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrcamentoOutput> buscarOrcamento(@PathVariable UUID id) {
        OrcamentoOutput orcamento = buscarOrcamentoUseCase.execute(id);
        return ResponseEntity.ok(orcamento);
    }

    @PatchMapping("/{id}/aprovar")
    public ResponseEntity<OrcamentoOutput> aprovarOrcamento(@PathVariable UUID id) {
        OrcamentoOutput orcamentoAprovado = aprovarOrcamentoUseCase.execute(id);
        return ResponseEntity.ok(orcamentoAprovado);
    }

    @PatchMapping("/{id}/rejeitar")
    public ResponseEntity<OrcamentoOutput> rejeitarOrcamento(@PathVariable UUID id) {
        OrcamentoOutput orcamentoRejeitado = rejeitarOrcamentoUseCase.execute(id);
        return ResponseEntity.ok(orcamentoRejeitado);
    }
}