package br.com.fiap.oficina_mecanica.ordemservico.infrastructure.ordemservico;

import br.com.fiap.oficina_mecanica.ordemservico.infrastructure.ordemservico.entity.HistoricoStatusOrdemServicoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface HistoricoStatusJpaRepository extends JpaRepository<HistoricoStatusOrdemServicoEntity, UUID> {

    @Query("""
            select avg((fim.dataTransicao - inicio.dataTransicao) by second) as mediaSegundos, count(inicio) as total
            from HistoricoStatusOrdemServicoEntity inicio, HistoricoStatusOrdemServicoEntity fim
            where inicio.ordemServicoId = fim.ordemServicoId
              and inicio.statusNovo = br.com.fiap.oficina_mecanica.ordemservico.domain.ordemservico.StatusOrdemServico.EM_EXECUCAO
              and fim.statusNovo = br.com.fiap.oficina_mecanica.ordemservico.domain.ordemservico.StatusOrdemServico.FINALIZADA
            """)
    TempoExecucaoAgregado calcularTempoExecucaoMedio();

    interface TempoExecucaoAgregado {
        Double getMediaSegundos();

        Long getTotal();
    }
}
