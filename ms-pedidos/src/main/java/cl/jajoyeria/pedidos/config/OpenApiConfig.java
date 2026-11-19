package cl.jajoyeria.pedidos.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConditionalOnProperty(
        name = "springdoc.api-docs.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class OpenApiConfig {

    @Bean
    public OpenAPI pedidosOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("JA Joyería · ms-pedidos API")
                        .version("0.1.0")
                        .description(
                                "API REST para la gestión de carritos, checkout, pagos simulados, "
                                        + "pedidos, despacho y reportes de ventas de JA Joyería."
                        )
                        .contact(new Contact()
                                .name("Equipo JA Joyería")
                                .email("contacto@jajoyeria.cl")))
                .servers(List.of(
                        new Server()
                                .url("/")
                                .description("Servidor actual")))
                .components(new Components()
                        .addSecuritySchemes("BearerAuth", new SecurityScheme()
                                .name("BearerAuth")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description(
                                        "Autenticación pendiente de configurar. Las operaciones "
                                                + "de cuenta y administración permanecen bloqueadas."
                                )));
    }
}