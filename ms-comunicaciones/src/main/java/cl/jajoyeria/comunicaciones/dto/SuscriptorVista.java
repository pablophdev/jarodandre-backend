package cl.jajoyeria.comunicaciones.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "Datos de un suscriptor activo al newsletter")
public record SuscriptorVista(
        @Schema(description = "ID del registro de suscripción", example = "10")
        Long id,

        @Schema(description = "Correo del suscriptor", example = "ana.soto@example.cl")
        String correo,

        @Schema(description = "Fecha y hora en que se dio de alta o reactivó", example = "2026-11-16T18:00:00Z")
        Instant altaEn) {}
