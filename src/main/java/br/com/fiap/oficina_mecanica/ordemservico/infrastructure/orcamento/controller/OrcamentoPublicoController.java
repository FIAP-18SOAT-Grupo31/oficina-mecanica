package br.com.fiap.oficina_mecanica.ordemservico.infrastructure.orcamento.controller;

import br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.dto.AprovacaoClienteInput;
import br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.dto.OrcamentoOutput;
import br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.usecase.AprovarOrcamentoClienteUseCase;
import br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.usecase.BuscarOrcamentoClienteUseCase;
import br.com.fiap.oficina_mecanica.ordemservico.application.orcamento.usecase.RejeitarOrcamentoClienteUseCase;
import br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.dto.AcessoClienteInput;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/publico/orcamentos")
public class OrcamentoPublicoController {

    private final BuscarOrcamentoClienteUseCase buscarOrcamentoClienteUseCase;
    private final AprovarOrcamentoClienteUseCase aprovarOrcamentoClienteUseCase;
    private final RejeitarOrcamentoClienteUseCase rejeitarOrcamentoClienteUseCase;

    public OrcamentoPublicoController(BuscarOrcamentoClienteUseCase buscarOrcamentoClienteUseCase,
                                      AprovarOrcamentoClienteUseCase aprovarOrcamentoClienteUseCase,
                                      RejeitarOrcamentoClienteUseCase rejeitarOrcamentoClienteUseCase) {
        this.buscarOrcamentoClienteUseCase = buscarOrcamentoClienteUseCase;
        this.aprovarOrcamentoClienteUseCase = aprovarOrcamentoClienteUseCase;
        this.rejeitarOrcamentoClienteUseCase = rejeitarOrcamentoClienteUseCase;
    }

    // POST mantém CPF e código de acesso fora da URL e dos logs de acesso.
    @PostMapping("/{id}/consultar")
    public ResponseEntity<OrcamentoOutput> consultar(@PathVariable UUID id,
                                                     @Valid @RequestBody AcessoClienteInput input) {
        return ResponseEntity.ok(buscarOrcamentoClienteUseCase.execute(id, input.cpf(), input.codigoAcesso()));
    }

    @PostMapping("/{id}/aprovar")
    public ResponseEntity<OrcamentoOutput> aprovar(@PathVariable UUID id,
                                                   @Valid @RequestBody AprovacaoClienteInput input) {
        return ResponseEntity.ok(aprovarOrcamentoClienteUseCase.execute(id, input));
    }

    @PostMapping("/{id}/rejeitar")
    public ResponseEntity<OrcamentoOutput> rejeitar(@PathVariable UUID id,
                                                    @Valid @RequestBody AcessoClienteInput input) {
        return ResponseEntity.ok(rejeitarOrcamentoClienteUseCase.execute(id, input.cpf(), input.codigoAcesso()));
    }
}
