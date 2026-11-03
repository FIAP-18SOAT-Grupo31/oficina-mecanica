package br.com.fiap.oficina_mecanica.estoque.infrastructure.controller;

import br.com.fiap.oficina_mecanica.compartilhado.application.dto.PaginaOutput;
import br.com.fiap.oficina_mecanica.estoque.application.dto.AjustarQuantidadeInput;
import br.com.fiap.oficina_mecanica.estoque.application.dto.AtualizarProdutoEstoqueInput;
import br.com.fiap.oficina_mecanica.estoque.application.dto.CriarProdutoEstoqueInput;
import br.com.fiap.oficina_mecanica.estoque.application.dto.EstoqueOutput;
import br.com.fiap.oficina_mecanica.estoque.application.usecase.AdicionarEstoqueUseCase;
import br.com.fiap.oficina_mecanica.estoque.application.usecase.AtualizarProdutoEstoqueUseCase;
import br.com.fiap.oficina_mecanica.estoque.application.usecase.BuscarProdutoEstoquePorIdUseCase;
import br.com.fiap.oficina_mecanica.estoque.application.usecase.BuscarProdutoEstoquePorNomeUseCase;
import br.com.fiap.oficina_mecanica.estoque.application.usecase.CriarProdutoEstoqueUseCase;
import br.com.fiap.oficina_mecanica.estoque.application.usecase.ExcluirProdutoEstoqueUseCase;
import br.com.fiap.oficina_mecanica.estoque.application.usecase.ListarProdutosEstoqueUseCase;
import br.com.fiap.oficina_mecanica.estoque.application.usecase.RemoverEstoqueUseCase;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.ResponseEntity.noContent;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.status;

@RestController
@RequestMapping("/api/estoque")
@Tag(name = "Peças e insumos")
public class EstoqueController {

    private final BuscarProdutoEstoquePorIdUseCase buscarProdutoEstoquePorIdUseCase;
    private final BuscarProdutoEstoquePorNomeUseCase buscarProdutoEstoquePorNomeUseCase;
    private final CriarProdutoEstoqueUseCase criarProdutoEstoqueUseCase;
    private final AdicionarEstoqueUseCase adicionarEstoqueUseCase;
    private final RemoverEstoqueUseCase removerEstoqueUseCase;
    private final ListarProdutosEstoqueUseCase listarProdutosEstoqueUseCase;
    private final AtualizarProdutoEstoqueUseCase atualizarProdutoEstoqueUseCase;
    private final ExcluirProdutoEstoqueUseCase excluirProdutoEstoqueUseCase;

    public EstoqueController(
            BuscarProdutoEstoquePorIdUseCase buscarProdutoEstoquePorIdUseCase,
            BuscarProdutoEstoquePorNomeUseCase buscarProdutoEstoquePorNomeUseCase,
            CriarProdutoEstoqueUseCase criarProdutoEstoqueUseCase,
            AdicionarEstoqueUseCase adicionarEstoqueUseCase,
            RemoverEstoqueUseCase removerEstoqueUseCase,
            ListarProdutosEstoqueUseCase listarProdutosEstoqueUseCase,
            AtualizarProdutoEstoqueUseCase atualizarProdutoEstoqueUseCase,
            ExcluirProdutoEstoqueUseCase excluirProdutoEstoqueUseCase) {
        this.buscarProdutoEstoquePorIdUseCase = buscarProdutoEstoquePorIdUseCase;
        this.buscarProdutoEstoquePorNomeUseCase = buscarProdutoEstoquePorNomeUseCase;
        this.criarProdutoEstoqueUseCase = criarProdutoEstoqueUseCase;
        this.adicionarEstoqueUseCase = adicionarEstoqueUseCase;
        this.removerEstoqueUseCase = removerEstoqueUseCase;
        this.listarProdutosEstoqueUseCase = listarProdutosEstoqueUseCase;
        this.atualizarProdutoEstoqueUseCase = atualizarProdutoEstoqueUseCase;
        this.excluirProdutoEstoqueUseCase = excluirProdutoEstoqueUseCase;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    public ResponseEntity<PaginaOutput<EstoqueOutput>> listar(
            @RequestParam(required = false) String nomePeca,
            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "deve ser maior ou igual a 0") int pagina,
            @RequestParam(defaultValue = "20")
            @Min(value = 1, message = "deve ser maior ou igual a 1")
            @Max(value = 100, message = "deve ser menor ou igual a 100") int tamanho) {
        return ok(listarProdutosEstoqueUseCase.execute(nomePeca, pagina, tamanho));
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

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    public ResponseEntity<EstoqueOutput> atualizarPeca(
            @PathVariable UUID id,
            @Valid @RequestBody AtualizarProdutoEstoqueInput input) {
        EstoqueOutput produtoAtualizado = atualizarProdutoEstoqueUseCase.execute(id, input);
        return ok(produtoAtualizado);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    public ResponseEntity<Void> excluirPeca(@PathVariable UUID id) {
        excluirProdutoEstoqueUseCase.execute(id);
        return noContent().build();
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
