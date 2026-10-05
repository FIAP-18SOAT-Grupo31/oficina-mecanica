package br.com.fiap.oficina_mecanica.ordemservico.infrastructure.orcamento.controller;

import br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.dto.GerarOrcamentoInput;
import br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.dto.OrcamentoOutput;
import br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.usecase.AprovarOrcamentoUseCase;
import br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.usecase.GerarOrcamentoUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/orcamentos")
public class OrcamentoController {

    private final GerarOrcamentoUseCase gerarOrcamentoUseCase;
    private final AprovarOrcamentoUseCase aprovarOrcamentoUseCase;

    public OrcamentoController(GerarOrcamentoUseCase gerarOrcamentoUseCase, AprovarOrcamentoUseCase aprovarOrcamentoUseCase) {
        this.gerarOrcamentoUseCase = gerarOrcamentoUseCase;
        this.aprovarOrcamentoUseCase = aprovarOrcamentoUseCase;
    }

    @PostMapping
    public ResponseEntity<OrcamentoOutput> gerarOrcamento(@RequestBody GerarOrcamentoInput input) {
        OrcamentoOutput orcamentoGerado = gerarOrcamentoUseCase.execute(input);
        return ResponseEntity.status(HttpStatus.CREATED).body(orcamentoGerado);
    }

    @PatchMapping("/{id}/aprovar")
    public ResponseEntity<OrcamentoOutput> aprovarOrcamento(@PathVariable UUID id) {
        OrcamentoOutput orcamentoAprovado = aprovarOrcamentoUseCase.execute(id);
        return ResponseEntity.ok(orcamentoAprovado);
    }
}