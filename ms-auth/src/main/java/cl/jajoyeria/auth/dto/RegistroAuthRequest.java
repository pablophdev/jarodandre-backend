package cl.jajoyeria.auth.dto;

import jakarta.validation.constraints.*;
import java.util.Locale;

public record RegistroAuthRequest(
        @NotBlank
        @Email
        @Pattern(regexp = "[^\\s@]+@[^\\s@]+\\.[^\\s@]+")
        @Size(max = 254)
        String correo,

        @NotBlank
        @Size(min = 8, max = 72)
        String password,

        @NotBlank
        @Size(max = 12)
        String rut,

        @NotBlank
        @Size(min = 3, max = 120)
        @Pattern(regexp = "[\\p{L}\\p{M} ]+")
        String nombre,

        @Pattern(regexp = "[0-9]{8,12}")
        String telefono,

        @NotNull
        @Positive
        Long comunaId
) {
    public RegistroAuthRequest {
        correo = correo == null
                ? null
                : correo.strip().toLowerCase(Locale.ROOT);

        nombre = nombre == null ? null : nombre.strip();
    }
}