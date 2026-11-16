package cl.jajoyeria.comunicaciones.dto;

import jakarta.validation.constraints.*;
import java.util.Locale;

public record MensajeRequest(
        @NotBlank @Size(max=120) @Pattern(regexp="[\\p{L}\\p{M}\\s]+") String nombre,
        @NotBlank @Email @Pattern(regexp="[^\\s@]+@[^\\s@]+\\.[^\\s@]+") @Size(max=254) String correo,
        @NotBlank @Size(max=2000) String mensaje) {
    public MensajeRequest {
        nombre = nombre == null ? null : nombre.strip();
        correo = correo == null ? null : correo.strip().toLowerCase(Locale.ROOT);
        mensaje = mensaje == null ? null : mensaje.strip();
    }
}
