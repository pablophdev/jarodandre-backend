package cl.jajoyeria.pedidos.dto;

import jakarta.validation.constraints.*;

public record Entrega(@NotBlank @Size(max = 120) String nombre, @NotBlank @Email @Size(max = 254) String correo,
        @NotBlank @Size(max = 250) String direccion, @NotBlank @Size(max = 100) String ciudad) {}
