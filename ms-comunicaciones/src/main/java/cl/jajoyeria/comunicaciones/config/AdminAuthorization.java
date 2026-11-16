package cl.jajoyeria.comunicaciones.config;

import java.time.Duration;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.authorization.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.client.*;

@Component
public class AdminAuthorization implements AuthorizationManager<RequestAuthorizationContext> {
    private final RestClient auth;

    public AdminAuthorization(RestClient.Builder builder, @Value("${app.auth.base-url}") String baseUrl) {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(2));
        factory.setReadTimeout(Duration.ofSeconds(3));
        auth = builder.baseUrl(baseUrl).requestFactory(factory).build();
    }

    private record Perfil(String id, String rol, String estado) {}

    @Override
    public AuthorizationDecision check(Supplier<Authentication> supplier, RequestAuthorizationContext context) {
        if (!(supplier.get() instanceof JwtAuthenticationToken bearer)) return new AuthorizationDecision(false);
        try {
            Perfil perfil = auth.get().uri("/v1/me")
                    .headers(h -> h.setBearerAuth(bearer.getToken().getTokenValue()))
                    .retrieve().body(Perfil.class);
            return new AuthorizationDecision(perfil != null && bearer.getToken().getSubject().equals(perfil.id())
                    && "ADMIN".equals(perfil.rol()) && "ACTIVO".equals(perfil.estado()));
        } catch (RestClientException ex) {
            return new AuthorizationDecision(false);
        }
    }
}
