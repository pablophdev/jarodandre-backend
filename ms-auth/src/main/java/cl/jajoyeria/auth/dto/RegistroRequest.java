package cl.jajoyeria.auth.dto;

import jakarta.validation.constraints.*;

public record RegistroRequest(
        @NotBlank @Size(max = 12) String rut,
        @NotBlank @Size(min = 3, max = 120) @Pattern(regexp = "[\\p{L}\\p{M} ]+") String nombre,
        @Pattern(regexp = "[0-9]{8,12}") String telefono,
        @NotNull @Positive Long comunaId) {}
