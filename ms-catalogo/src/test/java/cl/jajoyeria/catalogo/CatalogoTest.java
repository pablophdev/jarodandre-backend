package cl.jajoyeria.catalogo;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {"spring.profiles.active=local", "app.pedidos.client-id=pedidos-test",
        "spring.datasource.url=jdbc:h2:mem:catalogo;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE"})
@AutoConfigureMockMvc
@Transactional
class CatalogoTest {
    @Autowired MockMvc mvc;
    static HttpServer auth;

    @DynamicPropertySource
    static void configurar(DynamicPropertyRegistry registry) throws IOException {
        auth = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        auth.createContext("/v1/me", exchange -> {
            String header = exchange.getRequestHeaders().getFirst("Authorization");
            String rol = "Bearer admin".equals(header) ? "ADMIN" : "CLIENTE";
            byte[] json = ("{\"id\":\"usuario\",\"rol\":\"" + rol + "\",\"estado\":\"ACTIVO\"}").getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, json.length);
            try (var out = exchange.getResponseBody()) { out.write(json); }
        });
        auth.start();
        registry.add("app.media.dir", () -> imagenes.toString());
        registry.add("app.auth.base-url", () -> "http://127.0.0.1:" + auth.getAddress().getPort());
    }

    @org.junit.jupiter.api.io.TempDir
    static java.nio.file.Path imagenes;

    @Test
    void imagenYAltaDeProducto() throws Exception {
        var admin = jwt().jwt(j -> j.subject("usuario").tokenValue("admin"));
        byte[] foto;
        try (var stream = getClass().getResourceAsStream("/static/media/products/product-1.jpg")) {
            foto = stream.readAllBytes();
        }
        String resultado = mvc.perform(multipart("/v1/admin/imagenes")
                .file(new org.springframework.mock.web.MockMultipartFile("imagen","foto.jpg","image/jpeg",foto)).with(admin))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        String key = mapper.readTree(resultado).get("key").asText();
        mvc.perform(get("/media/" + key)).andExpect(status().isOk()).andExpect(content().bytes(foto));
        String producto = """
                {"categoriaId":1,"nombre":"Anillo de prueba","descripcion":"Diseño plata","precioClp":34000,
                 "imagenKey":"%s","etiquetas":["plata"],"grupo":"new","insignia":"","destacado":true,
                 "valoracion":0,"cantidadResenas":0,"disponibilidad":"CONSULTAR"}
                """.formatted(key);
        String creado = mvc.perform(post("/v1/admin/productos").with(admin).contentType("application/json").content(producto))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        long id = mapper.readTree(creado).get("id").longValue();
        org.junit.jupiter.api.Assertions.assertTrue(id > 14);
        mvc.perform(put("/v1/admin/productos/" + id).with(admin).contentType("application/json")
                .content(producto.replace("34000","35000"))).andExpect(jsonPath("$.precioClp").value(35000));
        mvc.perform(post("/v1/admin/productos").with(admin).contentType("application/json")
                .content(producto.replace("34000","-1"))).andExpect(status().isBadRequest());
    }

    @AfterAll
    static void apagar() { auth.stop(0); }

    @Test
    void cargaInicialYFiltros() throws Exception {
        mvc.perform(get("/v1/categorias")).andExpect(jsonPath("$.length()").value(4));
        mvc.perform(get("/v1/productos")).andExpect(jsonPath("$.totalElements").value(14));
        mvc.perform(get("/v1/productos").param("q","nacar flor")).andExpect(jsonPath("$.content[0].id").value(5));
        mvc.perform(get("/v1/productos/3")).andExpect(jsonPath("$.precioClp").value(105990));
        mvc.perform(get("/v1/publicaciones")).andExpect(jsonPath("$.length()").value(9));
        mvc.perform(get("/media/products/product-3.jpg")).andExpect(status().isOk());
    }

    @Test
    void permisosYArchivo() throws Exception {
        mvc.perform(delete("/v1/admin/productos/3")).andExpect(status().isUnauthorized());
        mvc.perform(delete("/v1/admin/productos/3").with(jwt().jwt(j -> j.subject("usuario").tokenValue("cliente"))))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/v1/admin/productos/3").with(jwt().jwt(j -> j.subject("usuario").tokenValue("admin"))))
                .andExpect(status().isNoContent());
        mvc.perform(get("/v1/productos/3")).andExpect(status().isNotFound());
    }

    @Test
    void borradorYPublicacion() throws Exception {
        var admin = jwt().jwt(j -> j.subject("usuario").tokenValue("admin"));
        String borrador = """
                {"slug":"nueva-joya","titulo":"Nueva joya","autorVisible":"Equipo JA","parrafos":["Texto"],
                 "portadaKey":"blog/blog-1.jpg","estado":"BORRADOR"}
                """;
        String body = mvc.perform(post("/v1/admin/publicaciones").with(admin).contentType("application/json").content(borrador))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        long id = new com.fasterxml.jackson.databind.ObjectMapper().readTree(body).get("id").longValue();
        mvc.perform(get("/v1/publicaciones/nueva-joya")).andExpect(status().isNotFound());
        mvc.perform(put("/v1/admin/publicaciones/" + id).with(admin).contentType("application/json")
                .content(borrador.replace("BORRADOR","PUBLICADO"))).andExpect(status().isOk());
        mvc.perform(get("/v1/publicaciones/nueva-joya")).andExpect(jsonPath("$.parrafos[0]").value("Texto"));
    }

    @Test
    void cotizacionProtegida() throws Exception {
        String data = "{\"productoIds\":[1,3]}";
        mvc.perform(post("/interno/v1/cotizaciones").contentType("application/json").content(data))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/interno/v1/cotizaciones").with(jwt().jwt(j -> j.claim("client_id","pedidos-test")).authorities(new SimpleGrantedAuthority("SCOPE_catalogo/cotizar")))
                .contentType("application/json").content(data)).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2));
    }
}
