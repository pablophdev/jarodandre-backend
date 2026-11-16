package cl.jajoyeria.pedidos;

import cl.jajoyeria.pedidos.service.*;
import cl.jajoyeria.pedidos.dto.Precio;
import com.fasterxml.jackson.databind.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.jwt.Jwt;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties={"spring.profiles.active=local","spring.datasource.url=jdbc:h2:mem:ventas;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE"})
@AutoConfigureMockMvc
class PedidosTest {
    @Autowired MockMvc api;
    @Autowired ObjectMapper json;
    @Autowired JdbcTemplate db;
    @MockitoBean CatalogoClient catalogo;
    @MockitoBean IdentidadService identidad;
    private static final String CART = "X-Carrito-Token", ORDER = "X-Pedido-Token", KEY = "Idempotency-Key";
    private static final String CHECKOUT = """
            {"entrega":{"nombre":"Ana Soto","correo":"ana@example.cl","direccion":"Los Álamos 18","ciudad":"Santiago"},"totalEsperadoClp":2000}
            """;

    @BeforeEach
    void precios() {
        when(catalogo.cotizar(List.of(3L))).thenReturn(Map.of(3L, new Precio(3L,"Anillo",1000L,"DISPONIBLE")));
    }

    private JsonNode body(ResultActions r) throws Exception {
        return json.readTree(r.andReturn().getResponse().getContentAsByteArray());
    }

    private String carrito() throws Exception {
        String token = body(api.perform(post("/v1/carrito")).andExpect(status().isOk())).get("tokenInvitado").asText();
        api.perform(put("/v1/carrito/items/3").header(CART,token).contentType("application/json").content("{\"cantidad\":2}"))
                .andExpect(status().isNoContent());
        return token;
    }

    private ResultActions checkout(String token, UUID clave, String datos) throws Exception {
        return api.perform(post("/v1/checkout").header(CART,token).header(KEY,clave).contentType("application/json").content(datos));
    }

    private ResultActions pagar(long id, String token, String accion) throws Exception {
        return api.perform(post("/v1/pedidos/"+id+"/pago-simulado").header(CART,token).contentType("application/json")
                .content("{\"accion\":\""+accion+"\"}"));
    }

    @Test
    void checkoutConcurrenteYPagoIdempotente() throws Exception {
        String token = carrito();
        UUID clave = UUID.randomUUID();
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            Callable<JsonNode> confirmar = () -> body(checkout(token,clave,CHECKOUT).andExpect(status().isOk()));
            var respuestas = executor.invokeAll(List.of(confirmar,confirmar));
            JsonNode uno = respuestas.get(0).get(), dos = respuestas.get(1).get();
            long id = uno.at("/pedido/id").asLong();
            assertEquals(id,dos.at("/pedido/id").asLong());
            assertEquals(uno.get("tokenConsulta"),dos.get("tokenConsulta"));
            Callable<Integer> simular = () -> pagar(id,token,"CONFIRMAR").andReturn().getResponse().getStatus();
            for (var r : executor.invokeAll(List.of(simular,simular))) assertEquals(200,r.get());
            assertEquals(1,db.queryForObject("select count(*) from ventas.intentos_pago where pedido_id=? and estado='PAGADO'",Integer.class,id));
            api.perform(get("/v1/carrito").header(CART,token)).andExpect(jsonPath("$.items.length()").value(0));
            api.perform(get("/v1/pedidos/"+id+"/comprobante").header(ORDER,uno.get("tokenConsulta").asText()))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.pedido.items[0].precioUnitarioClp").value(1000));
        }
    }

    @Test
    void precioModificadoCancelacionYReintento() throws Exception {
        String token=carrito();
        UUID clave=UUID.randomUUID();
        checkout(token,clave,CHECKOUT.replace("2000","1")).andExpect(status().isConflict());
        long id=body(checkout(token,clave,CHECKOUT).andExpect(status().isOk())).at("/pedido/id").asLong();
        checkout(token,clave,CHECKOUT.replace("Santiago","Temuco")).andExpect(status().isConflict());
        checkout(token,UUID.randomUUID(),CHECKOUT).andExpect(status().isConflict());
        api.perform(delete("/v1/carrito/items/3").header(CART,token)).andExpect(status().isConflict());
        pagar(id,token,"CANCELAR").andExpect(jsonPath("$.estadoPago").value("FALLIDO"));
        pagar(id,token,"CONFIRMAR").andExpect(status().isConflict());
        api.perform(get("/v1/carrito").header(CART,token)).andExpect(jsonPath("$.totalClp").value(2000));
        checkout(token,UUID.randomUUID(),CHECKOUT).andExpect(status().isOk());
    }

    @Test
    void privacidadVinculacionYRevocacion() throws Exception {
        String token=carrito();
        JsonNode orden=body(checkout(token,UUID.randomUUID(),CHECKOUT));
        long id=orden.at("/pedido/id").asLong();
        String lectura=orden.get("tokenConsulta").asText(), url="/v1/pedidos/"+id;
        api.perform(get(url)).andExpect(status().isNotFound());
        pagar(id,carrito(),"CONFIRMAR").andExpect(status().isNotFound());
        api.perform(get(url+"/comprobante").header(ORDER,lectura)).andExpect(status().isConflict());
        pagar(id,token,"CONFIRMAR").andExpect(status().isOk());
        UUID uid=UUID.randomUUID();
        var cuenta=jwt().jwt(j -> j.subject(uid.toString()));
        when(identidad.consultar(any(Jwt.class))).thenReturn(new IdentidadService.Perfil(uid,"otro@example.cl","CLIENTE","ACTIVO"));
        api.perform(post(url+"/vincular").with(cuenta).header(ORDER,lectura)).andExpect(status().isForbidden());
        when(identidad.consultar(any(Jwt.class))).thenReturn(new IdentidadService.Perfil(uid,"ana@example.cl","CLIENTE","ACTIVO"));
        api.perform(post(url+"/vincular").with(cuenta).header(ORDER,lectura)).andExpect(status().isOk());
        api.perform(get(url).header(ORDER,lectura)).andExpect(status().isNotFound());
        api.perform(get("/v1/mis-pedidos").with(cuenta)).andExpect(jsonPath("$.content[0].id").value((int)id));
    }

    @Test
    void despachoYReporte() throws Exception {
        String token=carrito();
        long id=body(checkout(token,UUID.randomUUID(),CHECKOUT)).at("/pedido/id").asLong();
        var enviar=patch("/v1/admin/pedidos/"+id+"/entrega").contentType("application/json").content("{\"estado\":\"ENVIADO\"}");
        api.perform(enviar).andExpect(status().isUnauthorized());
        var admin=jwt();
        api.perform(enviar.with(admin)).andExpect(status().isConflict());
        pagar(id,token,"CONFIRMAR").andExpect(status().isOk());
        api.perform(enviar.with(admin)).andExpect(jsonPath("$.estadoEntrega").value("ENVIADO"));
        api.perform(get("/v1/admin/reportes/ventas").with(admin)).andExpect(status().isOk())
                .andExpect(jsonPath("$.productosMasVendidos[0].productoId").value(3));
    }
}
