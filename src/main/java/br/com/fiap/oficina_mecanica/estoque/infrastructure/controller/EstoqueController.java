package br.com.fiap.oficina_mecanica.estoque.infrastructure.controller;

import br.com.fiap.oficina_mecanica.estoque.application.dto.AjustarQuantidadeInput;
import br.com.fiap.oficina_mecanica.estoque.application.dto.CriarProdutoEstoqueInput;
import br.com.fiap.oficina_mecanica.estoque.application.dto.EstoqueOutput;
import br.com.fiap.oficina_mecanica.estoque.application.usecase.BuscarProdutoEstoquePorIdUseCase;
import br.com.fiap.oficina_mecanica.estoque.application.usecase.BuscarProdutoEstoquePorNomeUseCase;
import br.com.fiap.oficina_mecanica.estoque.application.usecase.CriarProdutoEstoqueUseCase;
import br.com.fiap.oficina_mecanica.estoque.application.usecase.ReservarPecaUseCase;
import br.com.fiap.oficina_mecanica.estoque.application.usecase.RetirarPecaUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.status;

@RestController
@RequestMapping("/api/estoque")
public class EstoqueController {

    private final BuscarProdutoEstoquePorIdUseCase buscarProdutoEstoquePorIdUseCase;
    private final BuscarProdutoEstoquePorNomeUseCase buscarProdutoEstoquePorNomeUseCase;
    private final CriarProdutoEstoqueUseCase criarProdutoEstoqueUseCase;
    private final ReservarPecaUseCase reservarPecaUseCase;
    private final RetirarPecaUseCase retirarPecaUseCase;

    public EstoqueController(
            BuscarProdutoEstoquePorIdUseCase buscarProdutoEstoquePorIdUseCase,
            BuscarProdutoEstoquePorNomeUseCase buscarProdutoEstoquePorNomeUseCase,
            CriarProdutoEstoqueUseCase criarProdutoEstoqueUseCase,
            ReservarPecaUseCase reservarPecaUseCase,
            RetirarPecaUseCase retirarPecaUseCase) {
        this.buscarProdutoEstoquePorIdUseCase = buscarProdutoEstoquePorIdUseCase;
        this.buscarProdutoEstoquePorNomeUseCase = buscarProdutoEstoquePorNomeUseCase;
        this.criarProdutoEstoqueUseCase = criarProdutoEstoqueUseCase;
        this.reservarPecaUseCase = reservarPecaUseCase;
        this.retirarPecaUseCase = retirarPecaUseCase;
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    public ResponseEntity<EstoqueOutput> buscarPorId(@PathVariable UUID id) {
        EstoqueOutput produto = buscarProdutoEstoquePorIdUseCase.execute(id);
        return ok(produto);
    }

    @GetMapping("/peca")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    public ResponseEntity<EstoqueOutput> buscarPorNomePeca(@RequestParam String nomePeca) {
        EstoqueOutput produto = buscarProdutoEstoquePorNomeUseCase.execute(nomePeca);
        return ok(produto);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    public ResponseEntity<EstoqueOutput> criarPeca(@RequestBody CriarProdutoEstoqueInput input) {
        EstoqueOutput produtoCriado = criarProdutoEstoqueUseCase.execute(input);
        return status(CREATED).body(produtoCriado);
    }

    @PostMapping("/{id}/reservar")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    public ResponseEntity<EstoqueOutput> reservarPeca(
            @PathVariable UUID id,
            @RequestBody AjustarQuantidadeInput input) {
        EstoqueOutput produtoAtualizado = reservarPecaUseCase.execute(id, input);
        return ok(produtoAtualizado);
    }

    @DeleteMapping("/{id}/retirar")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    public ResponseEntity<EstoqueOutput> retirarPeca(
            @PathVariable UUID id,
            @RequestBody AjustarQuantidadeInput input) {
        EstoqueOutput produtoAtualizado = retirarPecaUseCase.execute(id, input);
        return ok(produtoAtualizado);
    }
}