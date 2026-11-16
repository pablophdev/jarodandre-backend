package cl.jajoyeria.pedidos.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

public record CheckoutRequest(@NotNull @Valid Entrega entrega, @NotNull @PositiveOrZero Long totalEsperadoClp) {}
