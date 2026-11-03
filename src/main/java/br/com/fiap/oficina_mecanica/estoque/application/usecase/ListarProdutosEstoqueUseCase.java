package br.com.fiap.oficina_mecanica.estoque.application.usecase;

import br.com.fiap.oficina_mecanica.compartilhado.application.dto.PaginaOutput;
import br.com.fiap.oficina_mecanica.estoque.application.dto.EstoqueOutput;
import br.com.fiap.oficina_mecanica.estoque.domain.ProdutoEstoqueRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListarProdutosEstoqueUseCase {

    private final ProdutoEstoqueRepository repository;

    public ListarProdutosEstoqueUseCase(ProdutoEstoqueRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public PaginaOutput<EstoqueOutput> execute(String nomePeca, int pagina, int tamanho) {
        String filtro = nomePeca == null || nomePeca.isBlank() ? null : nomePeca.trim();
        return PaginaOutput.de(repository.listar(filtro, pagina, tamanho).mapear(EstoqueOutput::deDominio));
    }
}
