package cl.jajoyeria.catalogo.dto;

import java.util.List;
import jakarta.validation.constraints.*;

public record CotizacionRequest(
        @NotEmpty @Size(max = 100) List<@NotNull @Positive Long> productoIds) {}
