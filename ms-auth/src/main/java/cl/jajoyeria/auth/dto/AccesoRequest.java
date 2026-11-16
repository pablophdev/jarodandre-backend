package cl.jajoyeria.auth.dto;

import cl.jajoyeria.auth.entity.*;
import jakarta.validation.constraints.NotNull;

public record AccesoRequest(
        @NotNull Rol rol, @NotNull Estado estado) {}
