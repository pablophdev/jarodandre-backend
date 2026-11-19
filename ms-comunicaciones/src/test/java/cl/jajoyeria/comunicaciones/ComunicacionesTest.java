package cl.jajoyeria.comunicaciones;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties={"spring.profiles.active=local",
        "spring.datasource.url=jdbc:h2:mem:comunicaciones;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE"})
@AutoConfigureMockMvc
class ComunicacionesTest {
    @Autowired MockMvc api;
    @Test
    void permitePreflightDelFrontend() throws Exception {
        api.perform(options("/v1/suscripciones")
                .header("Origin", "http://localhost:5173")
                .header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", "content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
    }
    @Autowired ObjectMapper json;
    static HttpServer auth;

    @DynamicPropertySource
    static void perfil(DynamicPropertyRegistry props) throws IOException {
        auth = HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
        auth.createContext("/v1/me", req -> {
            String token = req.getRequestHeaders().getFirst("Authorization");
            String rol = "Bearer cliente".equals(token) ? "CLIENTE" : "ADMIN";
            String estado = "Bearer bloqueado".equals(token) ? "BLOQUEADO" : "ACTIVO";
            byte[] body = ("{\"id\":\"usuario\",\"rol\":\""+rol+"\",\"estado\":\""+estado+"\"}").getBytes(StandardCharsets.UTF_8);
            req.getResponseHeaders().add("Content-Type","application/json");
            req.sendResponseHeaders(200, body.length);
            try (var stream = req.getResponseBody()) { stream.write(body); }
        });
        auth.start();
        props.add("app.auth.base-url", () -> "http://127.0.0.1:"+auth.getAddress().getPort());
    }

    @AfterAll
    static void cerrar() { auth.stop(0); }

    RequestPostProcessor identidad(String token) {
        return jwt().jwt(j -> j.subject("usuario").tokenValue(token));
    }

    @Test
    void validaFormulariosPublicos() throws Exception {
        api.perform(post("/v1/contacto").contentType("application/json")
                .content("{\"nombre\":\"Ana1\",\"correo\":\"invalido\",\"mensaje\":\" \"}")).andExpect(status().isBadRequest());
        api.perform(post("/v1/suscripciones").contentType("application/json")
                .content("{\"correo\":\"ana@local\"}")).andExpect(status().isBadRequest());
    }

    @Test
    void soloAdminActivoAccede() throws Exception {
        api.perform(get("/v1/admin/mensajes")).andExpect(status().isUnauthorized());
        api.perform(get("/v1/admin/suscriptores").with(identidad("cliente"))).andExpect(status().isForbidden());
        api.perform(get("/v1/admin/mensajes").with(identidad("bloqueado"))).andExpect(status().isForbidden());
        api.perform(get("/v1/admin/mensajes").with(identidad("admin"))).andExpect(status().isOk());
    }

    @Test
    void cicloMensaje() throws Exception {
        var admin = identidad("admin");
        api.perform(post("/v1/contacto").contentType("application/json")
                .content("{\"nombre\":\" María Soto \",\"correo\":\" ANA@example.cl \",\"mensaje\":\" Consulta por un anillo \"}"))
                .andExpect(status().isCreated());
        String lista = api.perform(get("/v1/admin/mensajes").with(admin).param("q","ANILLO").param("lectura","sin-leer"))
                .andExpect(jsonPath("$.content[0].correo").value("ana@example.cl")).andReturn().getResponse().getContentAsString();
        long id = json.readTree(lista).at("/content/0/id").asLong();
        var url = "/v1/admin/mensajes/"+id;
        api.perform(patch(url+"/lectura").with(admin).contentType("application/json").content("{\"leido\":true}")).andExpect(jsonPath("$.leidoEn").isString());
        api.perform(get("/v1/admin/mensajes/resumen").with(admin)).andExpect(jsonPath("$.sinLeer").value(0));
        api.perform(patch(url+"/lectura").with(admin).contentType("application/json").content("{\"leido\":false}")).andExpect(jsonPath("$.leidoEn").isEmpty());
        api.perform(delete(url).with(admin)).andExpect(status().isNoContent());
        api.perform(get("/v1/admin/mensajes").with(admin)).andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void concurrenciaBajaYReactivacion() throws Exception {
        var body = "{\"correo\":\"   ANA@Example.cl \"}";
        Callable<Integer> alta = () -> api.perform(post("/v1/suscripciones").contentType("application/json").content(body))
                .andReturn().getResponse().getStatus();
        try (var pool = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Integer> estados = new ArrayList<>();
            for (var resultado : pool.invokeAll(Collections.nCopies(6,alta))) estados.add(resultado.get());
            assertEquals(1,Collections.frequency(estados,201));
            assertEquals(5,Collections.frequency(estados,200));
        }
        var admin = identidad("admin");
        var lista = api.perform(get("/v1/admin/suscriptores").with(admin).param("q","ANA"))
                .andExpect(jsonPath("$.totalElements").value(1)).andReturn().getResponse().getContentAsString();
        long id = json.readTree(lista).at("/content/0/id").asLong();
        api.perform(delete("/v1/admin/suscriptores/"+id).with(admin)).andExpect(status().isNoContent());
        api.perform(get("/v1/admin/suscriptores").with(admin)).andExpect(jsonPath("$.totalElements").value(0));
        api.perform(post("/v1/suscripciones").contentType("application/json").content(body)).andExpect(jsonPath("$.estado").value("REACTIVADA"));
        api.perform(get("/v1/admin/suscriptores").with(admin)).andExpect(jsonPath("$.content[0].id").value((int)id));
    }

    @Test
    void swaggerYDocumentacionOpenApiDisponibles() throws Exception {
        api.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi").isString())
                .andExpect(jsonPath("$.info.title").value("JA Joyería · ms-comunicaciones API"))
                .andExpect(jsonPath("$.components.securitySchemes.BearerAuth").exists());

        api.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }
}
