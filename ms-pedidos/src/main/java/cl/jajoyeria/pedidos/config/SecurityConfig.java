package cl.jajoyeria.pedidos.config;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.*;
import org.springframework.http.HttpMethod;

@Configuration
public class SecurityConfig {
    @Bean
    SecurityFilterChain api(HttpSecurity http, @Value("${app.catalogo.internal-token}") String internalToken) throws Exception {
        return http.cors(cors -> {})
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(a -> a
                        .requestMatchers(HttpMethod.GET,"/interno/v1/compras/verificar").access((supplier,context)-> {
                            String token=context.getRequest().getHeader("X-Internal-Token");
                            boolean permitido=!internalToken.isBlank() && token!=null && java.security.MessageDigest.isEqual(
                                    internalToken.getBytes(java.nio.charset.StandardCharsets.UTF_8),token.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                            return new org.springframework.security.authorization.AuthorizationDecision(permitido);
                        })
                        .requestMatchers(HttpMethod.GET, "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/v1/admin/**", "/v1/mis-pedidos", "/v1/pedidos/*/vincular").authenticated()
                        .requestMatchers("/v1/carrito", "/v1/carrito/**", "/v1/checkout", "/v1/pedidos/*",
                                "/v1/pedidos/*/comprobante", "/v1/pedidos/*/pago-simulado").permitAll()
                        .anyRequest().denyAll())
                .oauth2ResourceServer(o -> o.jwt(jwt -> {}))
                .build();
    }



    @Bean
    CorsConfigurationSource corsConfigurationSource(@Value("${app.cors.origin}") String origin) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(origin));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Idempotency-Key", "X-Carrito-Token", "X-Pedido-Token"));
        config.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/v1/**", config);
        return source;
    }
}
