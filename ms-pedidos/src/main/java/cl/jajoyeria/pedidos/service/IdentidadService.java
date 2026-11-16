package cl.jajoyeria.pedidos.service;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.client.*;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.*;

@Service
public class IdentidadService {
    public record Perfil(UUID id, String correo, String rol, String estado) {}
    private final RestClient http;
    private final String url;

    public IdentidadService(RestClient http, @Value("${app.auth.url}") String url) {
        this.http = http;
        this.url = url;
    }

    public Perfil consultar(Jwt jwt) {
        if (jwt == null) return null;
        try {
            Perfil perfil = http.get().uri(url + "/v1/me").headers(h -> h.setBearerAuth(jwt.getTokenValue()))
                    .retrieve().body(Perfil.class);
            if (perfil == null || perfil.id() == null || !perfil.id().toString().equals(jwt.getSubject())
                    || !"ACTIVO".equals(perfil.estado())) throw new ResponseStatusException(FORBIDDEN);
            return perfil;
        } catch (HttpClientErrorException ex) {
            throw new ResponseStatusException(FORBIDDEN, "Perfil sin acceso");
        } catch (RestClientException ex) {
            throw new ResponseStatusException(SERVICE_UNAVAILABLE, "No se pudo verificar la cuenta");
        }
    }

    public void exigirAdmin(Jwt jwt) {
        Perfil perfil = consultar(jwt);
        if (perfil == null || !"ADMIN".equals(perfil.rol())) throw new ResponseStatusException(FORBIDDEN);
    }
}
