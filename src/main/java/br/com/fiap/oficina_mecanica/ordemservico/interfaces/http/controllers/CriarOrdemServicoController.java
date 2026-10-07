package br.com.fiap.oficina_mecanica.ordemservico.interfaces.http.controllers;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.dtos.CriarOrdemServicoInput;
import br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.dtos.CriarOrdemServicoOutput;
import br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.usecases.CriarOrdemServicoUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController 
@RequestMapping("/ordens-servico")
@RequiredArgsConstructor
public class CriarOrdemServicoController {

    private final CriarOrdemServicoUseCase criarOrdemServicoUseCase;

    @PostMapping 
    public ResponseEntity<Void> criar(@Valid @RequestBody CriarOrdemServicoInput input, UriComponentsBuilder uriBuilder) {
        CriarOrdemServicoOutput ordemServicoOutput = criarOrdemServicoUseCase.execute(input);

        URI location = uriBuilder.path("/ordens-servico/{id}")
                .buildAndExpand(ordemServicoOutput.ordemServicoId()).toUri();

        return ResponseEntity.created(location).build();
    }
}
