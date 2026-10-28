package br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.usecase;

import br.com.fiap.oficina_mecanica.compartilhado.domain.Pagina;
import br.com.fiap.oficina_mecanica.compartilhado.domain.Paginacao;
import br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.dto.ClienteResumoOutput;
import br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.dto.OrdemServicoResumoOutput;
import br.com.fiap.oficina_mecanica.ordemservico.application.ordemservico.dto.VeiculoResumoOutput;
import br.com.fiap.oficina_mecanica.ordemservico.domain.ordemservico.OrdemServico;
import br.com.fiap.oficina_mecanica.ordemservico.domain.ordemservico.OrdemServicoRepository;
import br.com.fiap.oficina_mecanica.ordemservico.domain.ordemservico.StatusOrdemServico;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ListarOrdensServicoUseCase {

    private final OrdemServicoRepository repository;
    private final ConsultorClientes consultorClientes;
    private final ConsultorVeiculos consultorVeiculos;

    public ListarOrdensServicoUseCase(OrdemServicoRepository repository, ConsultorClientes consultorClientes,
                                      ConsultorVeiculos consultorVeiculos) {
        this.repository = repository;
        this.consultorClientes = consultorClientes;
        this.consultorVeiculos = consultorVeiculos;
    }

    @Transactional(readOnly = true)
    public Pagina<OrdemServicoResumoOutput> execute(StatusOrdemServico status, Paginacao paginacao) {
        Pagina<OrdemServico> ordens = repository.listar(status, paginacao);

        Set<UUID> clienteIds = ordens.conteudo().stream().map(OrdemServico::getClienteId).collect(Collectors.toSet());
        Set<UUID> veiculoIds = ordens.conteudo().stream().map(OrdemServico::getVeiculoId).collect(Collectors.toSet());
        Map<UUID, ClienteResumoOutput> clientes = consultorClientes.resumirPorIds(clienteIds);
        Map<UUID, VeiculoResumoOutput> veiculos = consultorVeiculos.resumirPorIds(veiculoIds);

        return ordens.mapear(ordem -> OrdemServicoResumoOutput.de(
                ordem, clientes.get(ordem.getClienteId()), veiculos.get(ordem.getVeiculoId())));
    }
}
