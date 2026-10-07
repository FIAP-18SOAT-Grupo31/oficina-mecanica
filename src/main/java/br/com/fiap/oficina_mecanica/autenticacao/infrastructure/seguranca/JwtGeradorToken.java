package br.com.fiap.oficina_mecanica.autenticacao.infrastructure.seguranca;

import br.com.fiap.oficina_mecanica.autenticacao.application.dto.TokenOutput;
import br.com.fiap.oficina_mecanica.autenticacao.application.usecase.GeradorToken;
import br.com.fiap.oficina_mecanica.autenticacao.domain.Usuario;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class JwtGeradorToken implements GeradorToken {

    static final String EMISSOR = "oficina-mecanica";
    static final String CLAIM_PAPEIS = "roles";

    private final JwtEncoder jwtEncoder;
    private final PropriedadesJwt propriedades;

    public JwtGeradorToken(JwtEncoder jwtEncoder, PropriedadesJwt propriedades) {
        this.jwtEncoder = jwtEncoder;
        this.propriedades = propriedades;
    }

    @Override
    public TokenOutput gerar(Usuario usuario) {
        Instant agora = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(EMISSOR)
                .subject(usuario.getLogin())
                .issuedAt(agora)
                .expiresAt(agora.plus(propriedades.validade()))
                .claim(CLAIM_PAPEIS, List.of(usuario.getPapel().name()))
                .build();
        JwsHeader cabecalho = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(cabecalho, claims)).getTokenValue();
        return new TokenOutput(token, "Bearer", propriedades.validade().toSeconds());
    }
}
