package cl.jajoyeria.catalogo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta de carga de recurso multimedia")
public record ImagenResponse(
        @Schema(description = "Clave o ruta relativa asignada al recurso subido", example = "uploads/550e8400-e29b-41d4-a716-446655440000.jpg")
        String key) {}
