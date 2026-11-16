package cl.jajoyeria.pedidos.service;

import cl.jajoyeria.pedidos.dto.Precio;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;
import java.util.*;
import static org.springframework.http.HttpStatus.*;

@Service
public class CatalogoClient {
    private final RestClient http;
    private final String url, modo, tokenUrl, clientId, secret;
    private String token;
    private Instant renovar = Instant.EPOCH;
    private record Token(@JsonProperty("access_token") String valor, @JsonProperty("expires_in") long segundos) {}
    private record Ficha(Long id, String nombre, Long precioClp, String disponibilidad) {}
    private record Lote(List<Long> productoIds) {}

    public CatalogoClient(RestClient http, @Value("${app.catalogo.url}") String url,
            @Value("${app.catalogo.modo}") String modo, @Value("${app.catalogo.token-url}") String tokenUrl,
            @Value("${app.catalogo.client-id}") String clientId, @Value("${app.catalogo.client-secret}") String secret) {
        this.http = http;
        this.url = url;
        this.modo = modo;
        this.tokenUrl = tokenUrl;
        this.clientId = clientId;
        this.secret = secret;
    }

    private synchronized String credencial() {
        if (Instant.now().isBefore(renovar)) return token;
        var datos = new LinkedMultiValueMap<String,String>();
        datos.add("grant_type", "client_credentials");
        datos.add("scope", "catalogo/cotizar");
        Token t = http.post().uri(tokenUrl).headers(h -> h.setBasicAuth(clientId, secret))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED).body(datos).retrieve().body(Token.class);
        if (t == null || t.valor() == null || t.valor().isBlank()) throw new ResponseStatusException(BAD_GATEWAY);
        token = t.valor();
        renovar = Instant.now().plusSeconds(Math.max(0, t.segundos() - 30));
        return token;
    }

    public Map<Long, Precio> cotizar(List<Long> ids) {
        if (ids.isEmpty()) return Map.of();
        Map<Long, Precio> precios = new HashMap<>();
        try {
            if (modo.equals("publico-local")) {
                for (Long id : ids) {
                    try {
                        Ficha f = http.get().uri(url + "/v1/productos/{id}", id).retrieve().body(Ficha.class);
                        if (f != null && id.equals(f.id())) precios.put(id, new Precio(id, f.nombre(), f.precioClp(), f.disponibilidad()));
                    } catch (HttpClientErrorException.NotFound retirado) {
                        // El carrito conserva la línea para que el cliente pueda retirarla.
                    }
                }
            } else {
                if (tokenUrl.isBlank() || clientId.isBlank() || secret.isBlank()) throw new ResponseStatusException(SERVICE_UNAVAILABLE, "Faltan credenciales de catálogo");
                List<Precio> lote = http.post().uri(url + "/interno/v1/cotizaciones")
                        .headers(h -> h.setBearerAuth(credencial())).body(new Lote(ids)).retrieve()
                        .body(new ParameterizedTypeReference<List<Precio>>() {});
                if (lote == null) throw new ResponseStatusException(BAD_GATEWAY);
                for (Precio p : lote) precios.put(p.productoId(), p);
            }
        } catch (HttpClientErrorException.Conflict ex) {
            if (ids.size() == 1) return Map.of();
            int mitad = ids.size() / 2;
            precios.putAll(cotizar(ids.subList(0, mitad)));
            precios.putAll(cotizar(ids.subList(mitad, ids.size())));
        } catch (RestClientException ex) {
            throw new ResponseStatusException(SERVICE_UNAVAILABLE, "Catálogo no disponible");
        }
        return precios;
    }
}
