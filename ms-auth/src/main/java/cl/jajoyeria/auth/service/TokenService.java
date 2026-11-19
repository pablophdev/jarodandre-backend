package cl.jajoyeria.auth.service;

import cl.jajoyeria.auth.config.JwtConfig;
import cl.jajoyeria.auth.dto.TokenResponse;
import cl.jajoyeria.auth.dto.UsuarioResponse;
import com.nimbusds.jose.jwk.RSAKey;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenService {

    private static final long DURACION_SEGUNDOS = 900;

    private final JwtEncoder encoder;
    private final RSAKey rsaKey;

    public TokenResponse emitir(UsuarioResponse usuario) {
        Instant ahora = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(JwtConfig.ISSUER)
                .audience(List.of(JwtConfig.AUDIENCE))
                .subject(usuario.id().toString())
                .issuedAt(ahora)
                .expiresAt(ahora.plusSeconds(DURACION_SEGUNDOS))
                .id(UUID.randomUUID().toString())
                .claim("token_use", "access")
                .build();

        JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256)
                .keyId(rsaKey.getKeyID())
                .type("JWT")
                .build();

        String token = encoder.encode(
                JwtEncoderParameters.from(header, claims)
        ).getTokenValue();

        return new TokenResponse(
                token,
                "Bearer",
                DURACION_SEGUNDOS,
                usuario
        );
    }
}