package cl.jajoyeria.comunicaciones.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "Detalle de un mensaje de contacto en la bandeja administrativa")
public record MensajeVista(
        @Schema(description = "ID único del mensaje", example = "1")
        Long id,

        @Schema(description = "Nombre del remitente", example = "María Soto")
        String nombre,

        @Schema(description = "Correo del remitente", example = "maria.soto@example.cl")
        String correo,

        @Schema(description = "Texto completo del mensaje", example = "Hola, quisiera consultar por la disponibilidad de anillos en talla 6.")
        String mensaje,

        @Schema(description = "Fecha y hora de recepción del mensaje", example = "2026-11-16T20:30:00Z")
        Instant creadoEn,

        @Schema(description = "Fecha y hora en que fue marcado como leído, o null si está pendiente", example = "2026-11-16T20:35:00Z")
        Instant leidoEn) {}
