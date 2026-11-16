package cl.jajoyeria.comunicaciones.dto;

import jakarta.validation.constraints.*;
import java.util.Locale;

public record SuscripcionRequest(
        @NotBlank @Email @Pattern(regexp="[^\\s@]+@[^\\s@]+\\.[^\\s@]+") @Size(max=254) String correo) {
    public SuscripcionRequest {
        correo = correo == null ? null : correo.strip().toLowerCase(Locale.ROOT);
    }
}
