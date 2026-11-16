package cl.jajoyeria.catalogo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Categoría de joyas del catálogo")
public record CategoriaResponse(
        @Schema(description = "Identificador único de la categoría", example = "1")
        Long id,
        @Schema(description = "Nombre legible de la categoría", example = "Anillos")
        String nombre,
        @Schema(description = "Slug amigable para URLs y filtros", example = "anillos")
        String slug) {}
