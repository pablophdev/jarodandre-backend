package cl.jajoyeria.catalogo.dto;

import java.time.Instant;
import java.util.List;
import cl.jajoyeria.catalogo.entity.EstadoPublicacion;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Detalle de una publicación del blog")
public record PublicacionResponse(
        @Schema(description = "ID único de la publicación", example = "1")
        Long id,

        @Schema(description = "Slug URL amigable", example = "guia-cuidado-plata-925")
        String slug,

        @Schema(description = "Título de la publicación", example = "Guía completa para cuidar tus joyas de plata 925")
        String titulo,

        @Schema(description = "Firma o autor visible", example = "Equipo JA Joyería")
        String autorVisible,

        @Schema(description = "Párrafos del artículo")
        List<String> parrafos,

        @Schema(description = "Clave de la portada", example = "blog/blog-1.jpg")
        String portadaKey,

        @Schema(description = "Estado actual", example = "PUBLICADO", allowableValues = {"BORRADOR", "PUBLICADO"})
        EstadoPublicacion estado,

        @Schema(description = "Fecha y hora de publicación editorial", example = "2026-03-15T14:30:00Z")
        Instant publicadoEn) {}
