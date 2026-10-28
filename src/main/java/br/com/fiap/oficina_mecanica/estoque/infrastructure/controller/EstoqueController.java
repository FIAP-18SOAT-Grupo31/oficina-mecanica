package br.com.fiap.oficina_mecanica.estoque.infrastructure.controller;

import br.com.fiap.oficina_mecanica.estoque.application.dto.AjustarQuantidadeInput;
import br.com.fiap.oficina_mecanica.estoque.application.dto.CriarProdutoEstoqueInput;
import br.com.fiap.oficina_mecanica.estoque.application.dto.EstoqueOutput;
import br.com.fiap.oficina_mecanica.estoque.application.usecase.AdicionarEstoqueUseCase;
import br.com.fiap.oficina_mecanica.estoque.application.usecase.BuscarProdutoEstoquePorIdUseCase;
import br.com.fiap.oficina_mecanica.estoque.application.usecase.BuscarProdutoEstoquePorNomeUseCase;
import br.com.fiap.oficina_mecanica.estoque.application.usecase.CriarProdutoEstoqueUseCase;
import br.com.fiap.oficina_mecanica.estoque.application.usecase.RemoverEstoqueUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    private final AdicionarEstoqueUseCase adicionarEstoqueUseCase;
    private final RemoverEstoqueUseCase removerEstoqueUseCase;

    public EstoqueController(
            BuscarProdutoEstoquePorIdUseCase buscarProdutoEstoquePorIdUseCase,
            BuscarProdutoEstoquePorNomeUseCase buscarProdutoEstoquePorNomeUseCase,
            CriarProdutoEstoqueUseCase criarProdutoEstoqueUseCase,
            AdicionarEstoqueUseCase adicionarEstoqueUseCase,
            RemoverEstoqueUseCase removerEstoqueUseCase) {
        this.buscarProdutoEstoquePorIdUseCase = buscarProdutoEstoquePorIdUseCase;
        this.buscarProdutoEstoquePorNomeUseCase = buscarProdutoEstoquePorNomeUseCase;
        this.criarProdutoEstoqueUseCase = criarProdutoEstoqueUseCase;
        this.adicionarEstoqueUseCase = adicionarEstoqueUseCase;
        this.removerEstoqueUseCase = removerEstoqueUseCase;
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
    public ResponseEntity<EstoqueOutput> criarPeca(@Valid @RequestBody CriarProdutoEstoqueInput input) {
        EstoqueOutput produtoCriado = criarProdutoEstoqueUseCase.execute(input);
        return status(CREATED).body(produtoCriado);
    }

    @PostMapping("/{id}/adicionar")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    public ResponseEntity<EstoqueOutput> adicionarEstoque(
            @PathVariable UUID id,
            @Valid @RequestBody AjustarQuantidadeInput input) {
        EstoqueOutput produtoAtualizado = adicionarEstoqueUseCase.execute(id, input);
        return ok(produtoAtualizado);
    }

    @PostMapping("/{id}/remover")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    public ResponseEntity<EstoqueOutput> removerEstoque(
            @PathVariable UUID id,
            @Valid @RequestBody AjustarQuantidadeInput input) {
        EstoqueOutput produtoAtualizado = removerEstoqueUseCase.execute(id, input);
        return ok(produtoAtualizado);
    }
}