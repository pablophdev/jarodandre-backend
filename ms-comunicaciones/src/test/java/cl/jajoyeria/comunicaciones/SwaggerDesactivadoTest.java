package cl.jajoyeria.comunicaciones;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.profiles.active=postgres",
        "spring.datasource.url=jdbc:h2:mem:comunicadesact;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
        "app.auth.issuer=",
        "app.auth.client-id=",
        "app.auth.base-url=http://localhost:8081"
})
@AutoConfigureMockMvc
class SwaggerDesactivadoTest {
    @Autowired MockMvc api;

    @Test
    void swaggerDesactivadoEnPerfilPostgres() throws Exception {
        api.perform(get("/v3/api-docs")).andExpect(status().isNotFound());
        api.perform(get("/swagger-ui/index.html")).andExpect(status().isNotFound());
    }
}
