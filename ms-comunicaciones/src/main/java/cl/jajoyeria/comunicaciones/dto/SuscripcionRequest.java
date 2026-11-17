package cl.jajoyeria.comunicaciones.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.util.Locale;

@Schema(description = "Solicitud de suscripción al newsletter de la joyería")
public record SuscripcionRequest(
        @Schema(description = "Correo electrónico a suscribir", example = "ana.soto@example.cl")
        @NotBlank @Email @Pattern(regexp="[^\\s@]+@[^\\s@]+\\.[^\\s@]+") @Size(max=254) String correo) {
    public SuscripcionRequest {
        correo = correo == null ? null : correo.strip().toLowerCase(Locale.ROOT);
    }
}
