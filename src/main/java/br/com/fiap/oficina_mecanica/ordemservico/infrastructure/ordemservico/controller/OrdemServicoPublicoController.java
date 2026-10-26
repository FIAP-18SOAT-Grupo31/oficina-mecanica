package br.com.fiap.oficina_mecanica.ordemservico.infrastructure.ordemservico.controller;

import br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.dto.AcessoClienteInput;
import br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.dto.OrdemServicoClienteOutput;
import br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.usecase.ConsultarOrdemServicoClienteUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/publico/ordens-servico")
public class OrdemServicoPublicoController {

    private final ConsultarOrdemServicoClienteUseCase consultarOrdemServicoClienteUseCase;

    public OrdemServicoPublicoController(ConsultarOrdemServicoClienteUseCase consultarOrdemServicoClienteUseCase) {
        this.consultarOrdemServicoClienteUseCase = consultarOrdemServicoClienteUseCase;
    }

    // POST mantém CPF e código de acesso fora da URL e dos logs de acesso.
    @PostMapping("/{id}/consultar")
    public ResponseEntity<OrdemServicoClienteOutput> consultar(@PathVariable UUID id,
                                                               @Valid @RequestBody AcessoClienteInput input) {
        return ResponseEntity.ok(consultarOrdemServicoClienteUseCase.execute(id, input.cpf(), input.codigoAcesso()));
    }
}
