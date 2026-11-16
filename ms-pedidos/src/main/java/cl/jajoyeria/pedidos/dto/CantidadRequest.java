package cl.jajoyeria.pedidos.dto;

import jakarta.validation.constraints.*;

public record CantidadRequest(@NotNull @Min(1) @Max(99) Integer cantidad) {}
