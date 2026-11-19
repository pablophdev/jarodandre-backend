package cl.jajoyeria.comunicaciones.config;

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
    public OpenAPI comunicacionesOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("JA Joyería · ms-comunicaciones API")
                        .version("0.1.0")
                        .description("API REST para la recepción de mensajes de contacto de clientes, registro y bajas de suscripciones a newsletter, y gestión de bandeja de entrada administrativa de JA Joyería.")
                        .contact(new Contact().name("Equipo JA Joyería").email("contacto@jajoyeria.cl")))
                .servers(List.of(
                        new Server().url("http://localhost:8084").description("Servidor Local")))
                .components(new Components()
                        .addSecuritySchemes("BearerAuth", new SecurityScheme()
                                .name("BearerAuth")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Autenticación pendiente de configurar. La administración permanece bloqueada.")));
    }
}
