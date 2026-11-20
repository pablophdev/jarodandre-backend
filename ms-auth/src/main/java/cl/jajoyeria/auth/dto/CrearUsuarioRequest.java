package cl.jajoyeria.auth.dto;
import cl.jajoyeria.auth.entity.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record CrearUsuarioRequest(@NotNull @Valid RegistroAuthRequest registro,
        @NotNull Rol rol, @NotNull Estado estado) {}
