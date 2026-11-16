package cl.jajoyeria.comunicaciones.dto;

import jakarta.validation.constraints.NotNull;

public record LecturaRequest(@NotNull Boolean leido) {}
