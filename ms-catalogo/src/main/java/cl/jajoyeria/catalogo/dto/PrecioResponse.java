package cl.jajoyeria.catalogo.dto;

import cl.jajoyeria.catalogo.entity.Disponibilidad;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Cotización y disponibilidad vigente de una pieza para el checkout")
public record PrecioResponse(
        @Schema(description = "ID del producto", example = "3")
        Long productoId,

        @Schema(description = "Nombre oficial de la pieza", example = "Collar Flor de Nácar")
        String nombre,

        @Schema(description = "Precio vigente en Pesos Chilenos (CLP)", example = "105990")
        Long precioClp,

        @Schema(description = "Disponibilidad actual", example = "DISPONIBLE", allowableValues = {"DISPONIBLE", "AGOTADO", "CONSULTAR"})
        Disponibilidad disponibilidad) {}
