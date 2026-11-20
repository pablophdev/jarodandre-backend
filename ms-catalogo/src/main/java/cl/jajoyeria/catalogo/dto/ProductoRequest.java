package cl.jajoyeria.catalogo.dto;

import java.util.List;
import jakarta.validation.constraints.*;
import cl.jajoyeria.catalogo.entity.Disponibilidad;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Datos para crear o actualizar una pieza de joyería en el catálogo")
public record ProductoRequest(
        @Schema(description = "ID de la categoría a la que pertenece el producto", example = "1")
        @NotNull @Positive Long categoriaId,

        @Schema(description = "Nombre comercial de la pieza", example = "Anillo de Plata y Circones")
        @NotBlank @Size(max = 180) String nombre,

        @Schema(description = "Descripción detallada de la pieza, materiales y acabado", example = "Anillo elaborado en plata 925 con incrustaciones de circón brillante.")
        @NotBlank @Size(max = 8000) String descripcion,

        @Schema(description = "Precio vigente en Pesos Chilenos (CLP)", example = "35000")
        @NotNull @PositiveOrZero Long precioClp,

        @Schema(description = "Ruta relativa / clave de la imagen de portada", example = "products/product-1.jpg")
        @NotBlank @Size(max = 500) @Pattern(regexp = "(products|blog|uploads)/[A-Za-z0-9_-]+[.](jpg|jpeg|png|webp)") String imagenKey,

        @Schema(description = "Lista de etiquetas para búsqueda y clasificación", example = "[\"plata\", \"circones\", \"elegante\"]")
        @NotNull @Size(max = 40) List<@NotBlank @Size(max = 150) String> etiquetas,

        @Schema(description = "Grupo visual destacado en tienda ('new' o 'popular')", example = "new", allowableValues = {"new", "popular"})
        @NotNull @Pattern(regexp = "new|popular") String grupo,

        @Schema(description = "Texto de la insignia visual mostrada en la tarjeta", example = "Nuevo")
        @NotNull @Size(max = 80) String insignia,

        @Schema(description = "Indica si la pieza se destaca en la página de inicio", example = "true")
        @NotNull Boolean destacado,

        @Schema(description = "Estado de stock o disponibilidad", example = "DISPONIBLE", allowableValues = {"DISPONIBLE", "AGOTADO", "CONSULTAR"})
        @NotNull Disponibilidad disponibilidad) {}
