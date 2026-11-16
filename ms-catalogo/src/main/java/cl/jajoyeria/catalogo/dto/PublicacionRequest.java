package cl.jajoyeria.catalogo.dto;

import java.util.List;
import jakarta.validation.constraints.*;
import cl.jajoyeria.catalogo.entity.EstadoPublicacion;

public record PublicacionRequest(
        @NotBlank @Size(max = 180) @Pattern(regexp = "[a-z0-9]+(-[a-z0-9]+)*") String slug,
        @NotBlank @Size(max = 250) String titulo,
        @NotBlank @Size(max = 120) String autorVisible,
        @NotNull @Size(max = 100) List<@NotBlank @Size(max = 8000) String> parrafos,
        @NotBlank @Size(max = 500) @Pattern(regexp = "(products|blog|uploads)/[A-Za-z0-9_-]+[.](jpg|jpeg|png|webp)") String portadaKey,
        @NotNull EstadoPublicacion estado) {}
