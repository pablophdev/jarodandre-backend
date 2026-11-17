package cl.jajoyeria.auth.config;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.*;
import org.springframework.http.HttpMethod;

@Configuration
public class SecurityConfig {
    @Bean
    SecurityFilterChain api(HttpSecurity http) throws Exception {
        return http.cors(cors -> {})
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(a -> a
                        .requestMatchers(HttpMethod.GET, "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers(HttpMethod.GET, "/v1/regiones", "/v1/regiones/*/comunas").permitAll()
                        .requestMatchers("/v1/me", "/v1/admin/usuarios", "/v1/admin/usuarios/**").authenticated()
                        .anyRequest().denyAll())
                .oauth2ResourceServer(o -> o.jwt(jwt -> {}))
                .build();
    }

    @Bean
    JwtDecoder decoder(@Value("${app.auth.issuer}") String issuer,
                       @Value("${app.auth.client-id}") String clientId) {
        if ("unconfigured".equals(clientId)) {
            return token -> { throw new BadJwtException("Cognito sin configurar"); };
        }
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(issuer + "/.well-known/jwks.json").build();
        OAuth2TokenValidator<Jwt> claims = jwt -> {
            if ("access".equals(jwt.getClaimAsString("token_use"))
                    && clientId.equals(jwt.getClaimAsString("client_id"))
                    && jwt.getExpiresAt() != null && jwt.getSubject() != null) {
                return OAuth2TokenValidatorResult.success();
            }
            return OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token"));
        };
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(JwtValidators.createDefaultWithIssuer(issuer), claims));
        return decoder;
    }

    @Bean
    CorsConfigurationSource cors(@Value("${app.cors.origin}") String origin) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(origin));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setAllowedMethods(List.of("GET","POST","PUT","PATCH","OPTIONS"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/v1/**", config);
        return source;
    }
}
