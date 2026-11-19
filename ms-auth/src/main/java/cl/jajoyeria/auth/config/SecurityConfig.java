package cl.jajoyeria.auth.config;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.RSAKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.*;

import java.util.List;
import java.util.UUID;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain api(HttpSecurity http) throws Exception {
        return http
                .cors(cors -> {})
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(reglas -> reglas
                        .requestMatchers(
                                HttpMethod.GET,
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v1/regiones",
                                "/v1/regiones/*/comunas",
                                "/v1/auth/jwks"
                        ).permitAll()
                        .requestMatchers(
                                HttpMethod.POST,
                                "/v1/auth/registro",
                                "/v1/auth/login"
                        ).permitAll()
                        .requestMatchers(
                                "/v1/me",
                                "/v1/admin/usuarios",
                                "/v1/admin/usuarios/**"
                        ).authenticated()
                        .anyRequest().denyAll())
                .oauth2ResourceServer(oauth -> oauth.jwt(jwt -> {}))
                .build();
    }

    @Bean
    public JwtDecoder decoder(RSAKey rsaKey) throws JOSEException {
        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withPublicKey(rsaKey.toRSAPublicKey())
                .signatureAlgorithm(SignatureAlgorithm.RS256)
                .build();

        OAuth2TokenValidator<Jwt> contenido = jwt -> {
            boolean valido = jwt.getAudience().contains(JwtConfig.AUDIENCE)
                    && jwt.getExpiresAt() != null
                    && "access".equals(jwt.getClaimAsString("token_use"))
                    && sujetoValido(jwt.getSubject());

            if (valido) {
                return OAuth2TokenValidatorResult.success();
            }

            return OAuth2TokenValidatorResult.failure(
                    new OAuth2Error(
                            "invalid_token",
                            "El token no corresponde a un acceso válido de JA Joyería",
                            null
                    )
            );
        };

        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefaultWithIssuer(JwtConfig.ISSUER),
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

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors.origin}") String origen
    ) {
        CorsConfiguration configuracion = new CorsConfiguration();
        configuracion.setAllowedOrigins(List.of(origen));
        configuracion.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuracion.setAllowedMethods(
                List.of("GET", "POST", "PUT", "PATCH", "OPTIONS")
        );

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/v1/**", configuracion);
        return source;
    }
}