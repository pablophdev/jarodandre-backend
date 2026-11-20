package cl.jajoyeria.catalogo.service;

import java.time.Duration;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.client.*;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.*;

@Service
public class CompraVerificadaClient {
    public record Perfil(UUID id, String nombre, String estado) {}
    private record Compra(boolean compraPagada) {}
    private final RestClient http;
    private final String authUrl, pedidosUrl, secreto;

    public CompraVerificadaClient(RestClient.Builder builder,
            @Value("${app.auth.base-url}") String authUrl,
            @Value("${app.pedidos.url}") String pedidosUrl,
            @Value("${app.catalogo.internal-token}") String secreto) {
        var factory=new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(2));
        factory.setReadTimeout(Duration.ofSeconds(4));
        this.http=builder.requestFactory(factory).build();
        this.authUrl=authUrl;
        this.pedidosUrl=pedidosUrl;
        this.secreto=secreto;
    }

    public Perfil verificarPerfil(Jwt jwt) {
        try {
            Perfil perfil=http.get().uri(authUrl+"/v1/me").headers(h->h.setBearerAuth(jwt.getTokenValue()))
                    .retrieve().body(Perfil.class);
            if (perfil==null || perfil.id()==null || !perfil.id().toString().equals(jwt.getSubject()) || !"ACTIVO".equals(perfil.estado())) {
                throw new ResponseStatusException(FORBIDDEN,"Tu cuenta no tiene acceso activo.");
            }
            return perfil;
        } catch (HttpClientErrorException.Unauthorized | HttpClientErrorException.Forbidden ex) {
            throw new ResponseStatusException(FORBIDDEN,"Tu cuenta no tiene acceso activo.");
        } catch (RestClientException ex) {
            throw new ResponseStatusException(SERVICE_UNAVAILABLE,"No pudimos comprobar tu cuenta. Inténtalo más tarde.");
        }
    }

    public Perfil verificar(Jwt jwt, long productoId) {
        if (secreto.isBlank()) throw new ResponseStatusException(SERVICE_UNAVAILABLE,"No se pudo verificar la compra. Inténtalo más tarde.");
        try {
            Perfil perfil=verificarPerfil(jwt);
            Compra compra=http.get().uri(pedidosUrl+"/interno/v1/compras/verificar?usuarioId={usuario}&productoId={producto}",perfil.id(),productoId)
                    .header("X-Internal-Token",secreto).retrieve().body(Compra.class);
            if (compra==null || !compra.compraPagada()) throw new ResponseStatusException(FORBIDDEN,"Puedes valorar este producto después de comprarlo y confirmar el pago con tu cuenta.");
            return perfil;
        } catch (HttpClientErrorException.Unauthorized | HttpClientErrorException.Forbidden ex) {
            throw new ResponseStatusException(FORBIDDEN,"No pudimos autorizar tu cuenta o verificar la compra.");
        } catch (RestClientException ex) {
            throw new ResponseStatusException(SERVICE_UNAVAILABLE,"No pudimos verificar la compra. Inténtalo más tarde.");
        }
    }
}
