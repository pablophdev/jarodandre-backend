package cl.jajoyeria.comunicaciones.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.*;

import java.util.UUID;

@Configuration
public class AuthJwtConfig {

    private static final String ISSUER = "urn:ja-joyeria:ms-auth";
    private static final String AUDIENCE = "ja-joyeria";

    @Bean
    public JwtDecoder decoder(
            @Value("${app.auth.base-url}") String authUrl
    ) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withJwkSetUri(authUrl + "/v1/auth/jwks")
                .jwsAlgorithm(SignatureAlgorithm.RS256)
                .build();

        OAuth2TokenValidator<Jwt> contenido = jwt -> {
            boolean valido = jwt.getAudience().contains(AUDIENCE)
                    && jwt.getExpiresAt() != null
                    && "access".equals(jwt.getClaimAsString("token_use"))
                    && sujetoValido(jwt.getSubject());

            if (valido) {
                return OAuth2TokenValidatorResult.success();
            }

            return OAuth2TokenValidatorResult.failure(
                    new OAuth2Error(
                            "invalid_token",
                            "Token de acceso inválido para JA Joyería",
                            null
                    )
            );
        };

        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefaultWithIssuer(ISSUER),
                contenido
        ));

        return decoder;
    }

    private static boolean sujetoValido(String sujeto) {
        if (sujeto == null) {
            return false;
        }

        try {
            return UUID.fromString(sujeto).toString().equals(sujeto);
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}