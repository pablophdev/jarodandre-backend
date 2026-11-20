package cl.jajoyeria.catalogo.config;

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
    SecurityFilterChain api(HttpSecurity http, AdminAuthorization admin, @Value("${app.catalogo.internal-token}") String internalToken) throws Exception {
        return http.cors(cors -> {})
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(a -> a
                        .requestMatchers(HttpMethod.GET, "/v1/categorias", "/v1/productos", "/v1/productos/*",
                                "/v1/publicaciones", "/v1/publicaciones/*", "/v1/productos/*/resenas", "/media/**",
                                "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers(HttpMethod.GET,"/v1/grillz/opciones").permitAll()
                        .requestMatchers(HttpMethod.POST,"/v1/grillz/estimacion").permitAll()
                        .requestMatchers("/v1/grillz/disenos", "/v1/grillz/disenos/**").authenticated()
                        .requestMatchers(org.springframework.http.HttpMethod.PUT,"/v1/productos/*/resenas").authenticated()
                        .requestMatchers("/v1/admin/**").access(admin)
                        .requestMatchers(HttpMethod.POST, "/interno/v1/cotizaciones").access((supplier, context) -> {
                            String token = context.getRequest().getHeader("X-Internal-Token");
                            boolean permitido = !internalToken.isBlank() && token != null
                                    && java.security.MessageDigest.isEqual(internalToken.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                                            token.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                            return new org.springframework.security.authorization.AuthorizationDecision(permitido);
                        })
                        .anyRequest().denyAll())
                .oauth2ResourceServer(o -> o.jwt(jwt -> {}))
                .build();
    }



    @Bean
    CorsConfigurationSource corsConfigurationSource(@Value("${app.cors.origin}") String origin) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(origin));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/v1/**", config);
        return source;
    }
}
