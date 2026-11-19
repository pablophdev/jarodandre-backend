package cl.jajoyeria.auth.dto;

import jakarta.validation.constraints.*;
import java.util.Locale;

public record LoginRequest(
        @NotBlank
        @Email
        @Size(max = 254)
        String correo,

        @NotBlank
        @Size(max = 72)
        String password
) {
    public LoginRequest {
        correo = correo == null
                ? null
                : correo.strip().toLowerCase(Locale.ROOT);
    }
}