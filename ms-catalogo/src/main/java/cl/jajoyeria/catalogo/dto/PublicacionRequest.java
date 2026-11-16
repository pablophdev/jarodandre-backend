package cl.jajoyeria.catalogo.dto;

import java.util.List;
import jakarta.validation.constraints.*;
import cl.jajoyeria.catalogo.entity.EstadoPublicacion;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Datos para crear o editar un artículo del blog editorial")
public record PublicacionRequest(
        @Schema(description = "Slug amigable en minúsculas separado por guiones", example = "guia-cuidado-plata-925")
        @NotBlank @Size(max = 180) @Pattern(regexp = "[a-z0-9]+(-[a-z0-9]+)*") String slug,

        @Schema(description = "Título del artículo", example = "Guía completa para cuidar tus joyas de plata 925")
        @NotBlank @Size(max = 250) String titulo,

        @Schema(description = "Nombre o firma del autor visible para los lectores", example = "Equipo JA Joyería")
        @NotBlank @Size(max = 120) String autorVisible,

        @Schema(description = "Lista ordenada de párrafos que componen el contenido editorial", example = "[\"La plata es un metal noble que requiere cuidados regulares...\", \"Evita el contacto directo con perfumes y cremas...\"]")
        @NotNull @Size(max = 100) List<@NotBlank @Size(max = 8000) String> parrafos,

        @Schema(description = "Ruta relativa o clave de la portada", example = "blog/blog-1.jpg")
        @NotBlank @Size(max = 500) @Pattern(regexp = "(products|blog|uploads)/[A-Za-z0-9_-]+[.](jpg|jpeg|png|webp)") String portadaKey,

        @Schema(description = "Estado editorial de la publicación", example = "PUBLICADO", allowableValues = {"BORRADOR", "PUBLICADO"})
        @NotNull EstadoPublicacion estado) {}
