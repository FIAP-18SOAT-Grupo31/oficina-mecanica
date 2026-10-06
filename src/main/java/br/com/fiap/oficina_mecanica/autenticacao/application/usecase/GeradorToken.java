package br.com.fiap.oficina_mecanica.autenticacao.application.usecase;

import br.com.fiap.oficina_mecanica.autenticacao.application.dto.TokenOutput;
import br.com.fiap.oficina_mecanica.autenticacao.domain.Usuario;

public interface GeradorToken {
    TokenOutput gerar(Usuario usuario);
}
