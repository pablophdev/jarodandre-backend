package cl.jajoyeria.comunicaciones.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resultado del registro o reactivación de una suscripción al newsletter")
public record AltaRespuesta(
        @Schema(description = "Estado de la suscripción resultante", example = "ALTA", allowableValues = {"ALTA", "YA_ACTIVA", "REACTIVADA"})
        Estado estado) {
    public enum Estado { ALTA, YA_ACTIVA, REACTIVADA }
}
