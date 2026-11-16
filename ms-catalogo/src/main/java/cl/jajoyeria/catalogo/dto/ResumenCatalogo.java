package cl.jajoyeria.catalogo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resumen cuantitativo de productos del catálogo")
public record ResumenCatalogo(
        @Schema(description = "Total de productos visibles y activos en catálogo", example = "14")
        long productos,

        @Schema(description = "Total de productos marcados como destacados", example = "4")
        long destacados) {}
