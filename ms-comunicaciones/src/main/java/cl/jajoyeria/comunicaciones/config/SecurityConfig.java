package cl.jajoyeria.comunicaciones.config;

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
    SecurityFilterChain api(HttpSecurity http, AdminAuthorization admin) throws Exception {
        return http.cors(cors -> {})
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(a -> a
                        .requestMatchers(HttpMethod.POST, "/v1/contacto", "/v1/suscripciones").permitAll()
                        .requestMatchers(HttpMethod.GET, "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/v1/admin/**").access(admin)
                        .anyRequest().denyAll())
                .oauth2ResourceServer(o -> o.jwt(jwt -> {}))
                .build();
    }

    @Bean
    JwtDecoder decoder() {
        return token -> { throw new BadJwtException("Autenticación sin configurar"); };
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(@Value("${app.cors.origin}") String origin) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(origin));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setAllowedMethods(List.of("GET","POST","DELETE","PATCH","OPTIONS"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/v1/**", config);
        return source;
    }
}
