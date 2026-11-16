package cl.jajoyeria.catalogo.dto;

import java.util.List;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Solicitud de cotización en lote para ms-pedidos")
public record CotizacionRequest(
        @Schema(description = "Lista de IDs de productos a cotizar (máximo 100)", example = "[1, 3, 5]")
        @NotEmpty @Size(max = 100) List<@NotNull @Positive Long> productoIds) {}
