package cl.jajoyeria.pedidos.dto;

import jakarta.validation.constraints.NotNull;

public record PagoRequest(@NotNull Accion accion) {
    public enum Accion { CONFIRMAR, CANCELAR }
}
