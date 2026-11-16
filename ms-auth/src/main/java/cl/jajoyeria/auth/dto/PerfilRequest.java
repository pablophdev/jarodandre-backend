package cl.jajoyeria.auth.dto;

import jakarta.validation.constraints.*;

public record PerfilRequest(
        @NotBlank @Size(min = 3, max = 120) @Pattern(regexp = "[\\p{L}\\p{M} ]+") String nombre,
        @Pattern(regexp = "[0-9]{8,12}") String telefono,
        @NotNull @Positive Long comunaId) {}
