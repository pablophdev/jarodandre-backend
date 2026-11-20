package cl.jajoyeria.catalogo.dto;

import jakarta.validation.constraints.*;

public record ResenaRequest(@NotNull @Min(1) @Max(5) Integer estrellas,
        @Size(max=2000) String comentario) {}
