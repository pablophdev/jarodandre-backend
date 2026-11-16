package cl.jajoyeria.catalogo.dto;

import java.time.Instant;
import java.util.List;
import cl.jajoyeria.catalogo.entity.Disponibilidad;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ficha detallada de una pieza de joyería")
public record ProductoResponse(
        @Schema(description = "Identificador único del producto", example = "3")
        Long id,

        @Schema(description = "Categoría asignada a la pieza")
        CategoriaResponse categoria,

        @Schema(description = "Nombre comercial", example = "Collar Flor de Nácar")
        String nombre,

        @Schema(description = "Descripción completa de la joya", example = "Collar delicado con colgante de flor elaborado en nácar natural.")
        String descripcion,

        @Schema(description = "Precio vigente en Pesos Chilenos (CLP)", example = "105990")
        Long precioClp,

        @Schema(description = "Clave o ruta relativa de la imagen", example = "products/product-3.jpg")
        String imagenKey,

        @Schema(description = "Etiquetas asociadas", example = "[\"collar\", \"nacar\", \"flor\"]")
        List<String> etiquetas,

        @Schema(description = "Grupo visual ('new' o 'popular')", example = "popular")
        String grupo,

        @Schema(description = "Texto de la insignia visual", example = "Más vendido")
        String insignia,

        @Schema(description = "Si la pieza está destacada en portada", example = "true")
        boolean destacado,

        @Schema(description = "Valoración promedio (0 a 5)", example = "5")
        int valoracion,

        @Schema(description = "Total de reseñas acumuladas", example = "8")
        int cantidadResenas,

        @Schema(description = "Disponibilidad actual", example = "DISPONIBLE", allowableValues = {"DISPONIBLE", "AGOTADO", "CONSULTAR"})
        Disponibilidad disponibilidad,

        @Schema(description = "Marca de tiempo si la pieza fue archivada / dada de baja, o null si está activa", example = "null")
        Instant archivadoEn) {}
