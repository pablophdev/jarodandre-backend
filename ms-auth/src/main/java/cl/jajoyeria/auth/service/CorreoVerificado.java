package cl.jajoyeria.auth.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.client.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.Locale;

@Service
public class CorreoVerificado {
    private final RestClient client;
    private final String uri;

    public CorreoVerificado(RestClient.Builder builder, @Value("${app.auth.userinfo}") String uri) {
        this.client = builder.build();
        this.uri = uri;
    }

    private record UserInfo(String sub, String email, @JsonProperty("email_verified") boolean verified) {}

    public String obtener(Jwt token) {
        UserInfo info;
        try {
            info = client.get().uri(uri).headers(h -> h.setBearerAuth(token.getTokenValue()))
                    .retrieve().body(UserInfo.class);
        } catch (HttpClientErrorException ex) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cognito no autorizó la consulta del perfil");
        } catch (RestClientException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "No se pudo consultar Cognito");
        }
        if (info == null || !token.getSubject().equals(info.sub()) || !info.verified()
                || info.email() == null || info.email().isBlank()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Se requiere un correo verificado");
        }
        return info.email().strip().toLowerCase(Locale.ROOT);
    }
}
