package br.com.fiap.oficina_mecanica.ordemservico.infrastructure.ordemservico.controller;

import br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.dto.CriarOrdemServicoInput;
import br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.dto.CriarOrdemServicoOutput;
import br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.usecase.CriarOrdemServicoUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/ordens-servico")
public class OrdemServicoController {

    private final CriarOrdemServicoUseCase criarOrdemServicoUseCase;

    public OrdemServicoController(CriarOrdemServicoUseCase criarOrdemServicoUseCase) {
        this.criarOrdemServicoUseCase = criarOrdemServicoUseCase;
    }

    @PostMapping
    public ResponseEntity<CriarOrdemServicoOutput> criarOrdemServico(@Valid @RequestBody CriarOrdemServicoInput input,
                                                                     UriComponentsBuilder uriBuilder) {
        CriarOrdemServicoOutput ordemServicoCriada = criarOrdemServicoUseCase.execute(input);
        URI location = uriBuilder.path("/api/ordens-servico/{id}")
                .buildAndExpand(ordemServicoCriada.ordemServicoId())
                .toUri();
        return ResponseEntity.created(location).body(ordemServicoCriada);
    }
}
