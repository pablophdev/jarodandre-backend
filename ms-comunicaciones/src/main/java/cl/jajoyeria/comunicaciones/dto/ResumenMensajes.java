package cl.jajoyeria.comunicaciones.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resumen de estado de la bandeja de mensajes de contacto")
public record ResumenMensajes(
        @Schema(description = "Total histórico de mensajes recibidos", example = "42")
        long total,

        @Schema(description = "Cantidad de mensajes pendientes de lectura", example = "5")
        long sinLeer) {}
