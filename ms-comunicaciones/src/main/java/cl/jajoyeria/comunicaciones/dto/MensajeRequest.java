package cl.jajoyeria.comunicaciones.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.util.Locale;

@Schema(description = "Datos del formulario de contacto enviado por un cliente o visitante")
public record MensajeRequest(
        @Schema(description = "Nombre completo del remitente", example = "María Soto")
        @NotBlank @Size(max=120) @Pattern(regexp="[\\p{L}\\p{M}\\s]+") String nombre,

        @Schema(description = "Correo electrónico de contacto", example = "maria.soto@example.cl")
        @NotBlank @Email @Pattern(regexp="[^\\s@]+@[^\\s@]+\\.[^\\s@]+") @Size(max=254) String correo,

        @Schema(description = "Contenido o consulta del mensaje (hasta 2000 caracteres)", example = "Hola, quisiera consultar por la disponibilidad de anillos en talla 6.")
        @NotBlank @Size(max=2000) String mensaje) {
    public MensajeRequest {
        nombre = nombre == null ? null : nombre.strip();
        correo = correo == null ? null : correo.strip().toLowerCase(Locale.ROOT);
        mensaje = mensaje == null ? null : mensaje.strip();
    }
}
