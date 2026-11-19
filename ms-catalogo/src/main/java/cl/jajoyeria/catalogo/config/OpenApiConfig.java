package cl.jajoyeria.catalogo.config;

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
    public OpenAPI catalogoOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("JA Joyería · ms-catalogo API")
                        .version("0.1.0")
                        .description(
                                "API REST para la gestión del catálogo de joyas, categorías, "
                                        + "artículos del blog editorial, recursos multimedia "
                                        + "y cotizaciones internas en lote de JA Joyería."
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
                                        "Autenticación pendiente de configurar. "
                                                + "La administración permanece bloqueada."
                                ))
                        .addSecuritySchemes("PedidosServiceAuth", new SecurityScheme()
                                .name("X-Internal-Token")
                                .in(SecurityScheme.In.HEADER)
                                .type(SecurityScheme.Type.APIKEY)
                                .description(
                                        "Secreto exclusivo de catálogo y pedidos. "
                                                + "Nunca debe enviarse al frontend."
                                )));
    }
}