package cl.jajoyeria.catalogo.config;

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
    public OpenAPI catalogoOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("JA Joyería · ms-catalogo API")
                        .version("0.1.0")
                        .description("API REST para la gestión del catálogo de joyas, categorías, artículos del blog editorial, recursos multimedia y cotizaciones internas en lote de JA Joyería.")
                        .contact(new Contact().name("Equipo JA Joyería").email("contacto@jajoyeria.cl")))
                .servers(List.of(
                        new Server().url("http://localhost:8082").description("Servidor Local")))
                .components(new Components()
                        .addSecuritySchemes("BearerAuth", new SecurityScheme()
                                .name("BearerAuth")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Token Bearer JWT emitido por AWS Cognito. Requerido para operaciones de administración (/v1/admin/**) con rol ADMIN y estado ACTIVO."))
                        .addSecuritySchemes("PedidosServiceAuth", new SecurityScheme()
                                .name("PedidosServiceAuth")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Token OAuth2 M2M con scope 'catalogo/cotizar' para ms-pedidos (/interno/v1/cotizaciones).")));
    }
}
