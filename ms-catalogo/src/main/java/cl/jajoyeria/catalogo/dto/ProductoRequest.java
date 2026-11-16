package cl.jajoyeria.catalogo.dto;

import java.util.List;
import jakarta.validation.constraints.*;
import cl.jajoyeria.catalogo.entity.Disponibilidad;

public record ProductoRequest(
        @NotNull @Positive Long categoriaId,
        @NotBlank @Size(max = 180) String nombre,
        @NotBlank @Size(max = 8000) String descripcion,
        @NotNull @PositiveOrZero Long precioClp,
        @NotBlank @Size(max = 500) @Pattern(regexp = "(products|blog|uploads)/[A-Za-z0-9_-]+[.](jpg|jpeg|png|webp)") String imagenKey,
        @NotNull @Size(max = 40) List<@NotBlank @Size(max = 150) String> etiquetas,
        @NotNull @Pattern(regexp = "new|popular") String grupo,
        @NotNull @Size(max = 80) String insignia,
        @NotNull Boolean destacado,
        @NotNull @Min(0) @Max(5) Integer valoracion,
        @NotNull @PositiveOrZero Integer cantidadResenas,
        @NotNull Disponibilidad disponibilidad) {}
