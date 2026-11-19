package cl.jajoyeria.pedidos.service;

import cl.jajoyeria.pedidos.dto.Precio;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
import static org.springframework.http.HttpStatus.*;

@Service
public class CatalogoClient {
    private final RestClient http;
    private final String url, internalToken;
    private record Lote(List<Long> productoIds) {}

    public CatalogoClient(RestClient http, @Value("${app.catalogo.url}") String url,
            @Value("${app.catalogo.internal-token}") String internalToken) {
        this.http = http;
        this.url = url;
        this.internalToken = internalToken;
    }

    public Map<Long, Precio> cotizar(List<Long> ids) {
        if (ids.isEmpty()) return Map.of();
        if (internalToken.isBlank()) throw new ResponseStatusException(SERVICE_UNAVAILABLE, "Configura CATALOGO_INTERNAL_TOKEN en catálogo y pedidos");
        Map<Long, Precio> precios = new HashMap<>();
        try {
            List<Precio> lote = http.post().uri(url + "/interno/v1/cotizaciones")
                    .header("X-Internal-Token", internalToken).body(new Lote(ids)).retrieve()
                    .body(new ParameterizedTypeReference<List<Precio>>() {});
            if (lote == null) throw new ResponseStatusException(BAD_GATEWAY);
            for (Precio p : lote) precios.put(p.productoId(), p);
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
