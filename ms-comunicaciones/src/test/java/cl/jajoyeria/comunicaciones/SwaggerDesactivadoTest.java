package cl.jajoyeria.comunicaciones;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "springdoc.api-docs.enabled=false",
        "springdoc.swagger-ui.enabled=false"
})
@AutoConfigureMockMvc
class SwaggerDesactivadoTest extends PostgresIntegrationTest {
    @Autowired MockMvc api;

    @Test
    void swaggerDesactivadoEnPerfilPostgres() throws Exception {
        api.perform(get("/v3/api-docs")).andExpect(status().isNotFound());
        api.perform(get("/swagger-ui/index.html")).andExpect(status().isNotFound());
    }
}
