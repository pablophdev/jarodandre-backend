package cl.jajoyeria.pedidos.dto;

import jakarta.validation.constraints.NotNull;
import cl.jajoyeria.pedidos.entity.EstadoEntrega;

public record EntregaRequest(@NotNull EstadoEntrega estado) {}
