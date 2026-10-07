package br.com.fiap.oficina_mecanica.autenticacao.infrastructure.controller;

import br.com.fiap.oficina_mecanica.autenticacao.application.dto.LoginInput;
import br.com.fiap.oficina_mecanica.autenticacao.application.dto.TokenOutput;
import br.com.fiap.oficina_mecanica.autenticacao.application.usecase.AutenticarUsuarioUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticação")
public class AutenticacaoController {

    private final AutenticarUsuarioUseCase autenticarUsuarioUseCase;

    public AutenticacaoController(AutenticarUsuarioUseCase autenticarUsuarioUseCase) {
        this.autenticarUsuarioUseCase = autenticarUsuarioUseCase;
    }

    @PostMapping("/login")
    @Operation(summary = "Gera o token JWT das APIs administrativas")
    public ResponseEntity<TokenOutput> login(@Valid @RequestBody LoginInput input) {
        return ResponseEntity.ok(autenticarUsuarioUseCase.execute(input));
    }
}
