package cl.jajoyeria.comunicaciones.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Petición para cambiar el estado de lectura de un mensaje de contacto")
public record LecturaRequest(
        @Schema(description = "Indica si el mensaje pasa a estar leído (true) o no leído (false)", example = "true")
        @NotNull Boolean leido) {}
