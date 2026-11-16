package cl.jajoyeria.catalogo.config;

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
    SecurityFilterChain api(HttpSecurity http, AdminAuthorization admin, @Value("${app.pedidos.client-id}") String pedidosId) throws Exception {
        return http.cors(cors -> {})
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(a -> a
                        .requestMatchers(HttpMethod.GET, "/v1/categorias", "/v1/productos", "/v1/productos/*",
                                "/v1/publicaciones", "/v1/publicaciones/*", "/media/**").permitAll()
                        .requestMatchers("/v1/admin/**").access(admin)
                        .requestMatchers(HttpMethod.POST, "/interno/v1/cotizaciones").access((supplier, context) -> {
                            var auth = supplier.get();
                            boolean permitido = auth instanceof org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken token
                                    && !pedidosId.isBlank() && pedidosId.equals(token.getToken().getClaimAsString("client_id"))
                                    && token.getAuthorities().stream().anyMatch(authority -> authority.getAuthority().equals("SCOPE_catalogo/cotizar"));
                            return new org.springframework.security.authorization.AuthorizationDecision(permitido);
                        })
                        .anyRequest().denyAll())
                .oauth2ResourceServer(o -> o.jwt(jwt -> {}))
                .build();
    }

    @Bean
    JwtDecoder decoder(@Value("${app.auth.issuer}") String issuer,
                       @Value("${app.auth.client-id}") String clientId,
                       @Value("${app.pedidos.client-id:}") String pedidosId) {
        if ("unconfigured".equals(clientId)) {
            return token -> { throw new BadJwtException("Cognito sin configurar"); };
        }
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(issuer + "/.well-known/jwks.json").build();
        OAuth2TokenValidator<Jwt> claims = jwt -> {
            if ("access".equals(jwt.getClaimAsString("token_use"))
                    && (clientId.equals(jwt.getClaimAsString("client_id")) || (!pedidosId.isBlank() && pedidosId.equals(jwt.getClaimAsString("client_id"))))
                    && jwt.getExpiresAt() != null) {
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
        config.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/v1/**", config);
        return source;
    }
}
