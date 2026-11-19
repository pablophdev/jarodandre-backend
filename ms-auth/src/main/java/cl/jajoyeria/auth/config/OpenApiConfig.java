package cl.jajoyeria.auth.config;

import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.*;
import io.swagger.v3.oas.models.security.*;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.List;

@Configuration
@ConditionalOnProperty(name = "springdoc.api-docs.enabled", havingValue = "true", matchIfMissing = true)
public class OpenApiConfig {

    @Bean
    public OpenAPI authOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("JA Joyería · ms-auth API")
                        .version("0.1.0")
                        .description("API REST para la consulta de territorios, gestión de perfiles de clientes y administración de roles y estados de acceso de JA Joyería.")
                        .contact(new Contact().name("Equipo JA Joyería").email("contacto@jajoyeria.cl")))
                .servers(List.of(
                        new Server().url("http://localhost:8081").description("Servidor Local")))
                .components(new Components()
                        .addSecuritySchemes("BearerAuth", new SecurityScheme()
                                .name("BearerAuth")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Autenticación pendiente de configurar. Las operaciones privadas y administrativas permanecen bloqueadas.")));
    }
}
