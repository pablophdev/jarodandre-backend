package cl.jajoyeria.pedidos;

import cl.jajoyeria.pedidos.service.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;
import static org.springframework.test.web.client.ExpectedCount.twice;

class ClientesHttpTest {
    @Test
    void cotizacionSinSecretoFallaCerrada() {
        var catalogo = new CatalogoClient(RestClient.create(), "https://catalogo.test", "");
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE,
                assertThrows(ResponseStatusException.class, () -> catalogo.cotizar(List.of(3L))).getStatusCode());
    }

    @Test
    void credencialesDeServicioYCotizacionPorLote() {
        var builder = RestClient.builder();
        var server = MockRestServiceServer.bindTo(builder).build();
        var catalogo = new CatalogoClient(builder.build(), "https://catalogo.test", "test-internal-token");
        server.expect(twice(),requestTo("https://catalogo.test/interno/v1/cotizaciones"))
                .andExpect(header("X-Internal-Token","test-internal-token"))
                .andExpect(content().json("{\"productoIds\":[3]}"))
                .andRespond(withSuccess("[{\"productoId\":3,\"nombre\":\"Anillo\",\"precioClp\":1000,\"disponibilidad\":\"DISPONIBLE\"}]",MediaType.APPLICATION_JSON));
        for (int i=0; i<2; i++) assertEquals(1000L,catalogo.cotizar(List.of(3L)).get(3L).precioClp());
        server.verify();
    }

    @Test
    void perfilBloqueadoYClienteSinPermisoAdministrativo() {
        var builder = RestClient.builder();
        var server = MockRestServiceServer.bindTo(builder).build();
        var auth = new IdentidadService(builder.build(),"https://auth.test");
        UUID id = UUID.randomUUID();
        var jwt = Jwt.withTokenValue("perfil").header("alg","RS256").subject(id.toString()).build();
        for (String estado : List.of("ACTIVO","BLOQUEADO")) {
            server.expect(requestTo("https://auth.test/v1/me")).andExpect(header("Authorization","Bearer perfil"))
                    .andRespond(withSuccess("{\"id\":\""+id+"\",\"rol\":\"CLIENTE\",\"estado\":\""+estado+"\"}",MediaType.APPLICATION_JSON));
        }
        assertEquals(HttpStatus.FORBIDDEN,assertThrows(ResponseStatusException.class,() -> auth.exigirAdmin(jwt)).getStatusCode());
        assertEquals(HttpStatus.FORBIDDEN,assertThrows(ResponseStatusException.class,() -> auth.consultar(jwt)).getStatusCode());
        server.verify();
    }
}
