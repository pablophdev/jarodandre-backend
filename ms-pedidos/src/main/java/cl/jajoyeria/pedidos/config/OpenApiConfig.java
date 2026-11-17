package cl.jajoyeria.pedidos.config;

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
    public OpenAPI pedidosOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("JA Joyería · ms-pedidos API")
                        .version("0.1.0")
                        .description("API REST para la gestión de carritos, checkout, pagos simulados, pedidos, despacho y reportes de ventas de JA Joyería.")
                        .contact(new Contact().name("Equipo JA Joyería").email("contacto@jajoyeria.cl")))
                .servers(List.of(
                        new Server().url("http://localhost:8083").description("Servidor Local")))
                .components(new Components()
                        .addSecuritySchemes("BearerAuth", new SecurityScheme()
                                .name("BearerAuth")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Token Bearer JWT emitido por AWS Cognito. Requerido para operaciones de cuenta y administración.")));
    }
}
