package cl.jajoyeria.auth;

import cl.jajoyeria.auth.service.CorreoVerificado;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class IdentidadTest extends PostgresIntegrationTest {
    @Autowired MockMvc mvc;
    @Test
    void permitePreflightDelFrontend() throws Exception {
        mvc.perform(options("/v1/me")
                .header("Origin", "http://localhost:5173")
                .header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", "authorization,content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
        mvc.perform(options("/v1/me")
                .header("Origin", "https://origen-ajeno.example")
                .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isForbidden());
    }
    @Autowired JdbcTemplate jdbc;
    @MockitoBean CorreoVerificado correo;

    private static final String ID = "015d5f49-c538-4d59-b463-ac5c0a447c99";
    private static final String OTRO = "015d5f49-c538-4d59-b463-ac5c0a447c98";
    private static final String PERFIL = """
            {"rut":"12.345.678-5","nombre":"Ana Soto","comunaId":1}
            """;

    @Test
    void territoriosPublicosYPerfilesProtegidos() throws Exception {
        mvc.perform(get("/v1/regiones")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(3));
        mvc.perform(get("/v1/regiones/RM/comunas")).andExpect(jsonPath("$.length()").value(5));
        mvc.perform(get("/v1/me")).andExpect(status().isUnauthorized());
        mvc.perform(get("/v1/me").header("Authorization","Bearer inventado")).andExpect(status().isUnauthorized());
    }

    @Test
    void registroAutorizacionYConflictos() throws Exception {
        when(correo.obtener(any())).thenReturn("ana@example.cl", "otro@example.cl");
        var cliente = jwt().jwt(j -> j.subject(ID));
        var segundo = jwt().jwt(j -> j.subject(OTRO));
        mvc.perform(post("/v1/me").with(cliente).contentType("application/json").content(PERFIL))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.rut").value("12345678-5"))
                .andExpect(jsonPath("$.rol").value("CLIENTE"));
        mvc.perform(post("/v1/me").with(segundo).contentType("application/json").content(PERFIL))
                .andExpect(status().isConflict());
        mvc.perform(get("/v1/me").with(cliente)).andExpect(status().isOk());
        mvc.perform(get("/v1/admin/usuarios").with(cliente)).andExpect(status().isForbidden());
        jdbc.update("UPDATE identidad.usuarios SET rol='ADMIN' WHERE id=?", java.util.UUID.fromString(ID));
        mvc.perform(get("/v1/admin/usuarios").with(cliente)).andExpect(status().isOk());
        jdbc.update("UPDATE identidad.usuarios SET estado='BLOQUEADO' WHERE id=?", java.util.UUID.fromString(ID));
        mvc.perform(get("/v1/me").with(cliente)).andExpect(status().isForbidden());
    }

    @Test
    void rechazaRutSinDigitoVerificadorValido() throws Exception {
        when(correo.obtener(any())).thenReturn("prueba@example.cl");
        mvc.perform(post("/v1/me").with(jwt().jwt(j -> j.subject(OTRO)))
                        .contentType("application/json").content(PERFIL.replace("678-5","678-4")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void swaggerYDocumentacionOpenApiDisponibles() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi").isString())
                .andExpect(jsonPath("$.info.title").value("JA Joyería · ms-auth API"))
                .andExpect(jsonPath("$.components.securitySchemes.BearerAuth").exists())
                .andExpect(jsonPath("$.paths['/v1/me'].post").exists())
                .andExpect(jsonPath("$.paths['/v1/admin/usuarios/{id}/acceso'].patch").exists());

        mvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }
}
