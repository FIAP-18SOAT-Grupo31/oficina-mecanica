package br.com.fiap.oficina_mecanica.servico.application.usecase;

import br.com.fiap.oficina_mecanica.compartilhado.domain.exception.RegraNegocioException;
import br.com.fiap.oficina_mecanica.servico.application.dto.CriarServicoInput;
import br.com.fiap.oficina_mecanica.servico.application.dto.ServicoOutput;
import br.com.fiap.oficina_mecanica.servico.domain.Servico;
import br.com.fiap.oficina_mecanica.servico.domain.ServicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CriarServicoUseCase {

    private final ServicoRepository repository;

    public CriarServicoUseCase(ServicoRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public ServicoOutput execute(CriarServicoInput input) {
        Servico servico = montarServico(input);

        if (repository.existePorNome(servico.getNome())) {
            throw new RegraNegocioException("Já existe um serviço cadastrado com este nome.");
        }

        return ServicoOutput.de(repository.salvar(servico));
    }

    private static Servico montarServico(CriarServicoInput input) {
        try {
            return new Servico(input.nome(), input.descricao(), input.valorMaoDeObra());
        } catch (IllegalArgumentException ex) {
            throw new RegraNegocioException(ex.getMessage());
        }
    }
}
