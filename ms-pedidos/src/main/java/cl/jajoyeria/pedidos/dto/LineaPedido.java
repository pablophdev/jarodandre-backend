package cl.jajoyeria.pedidos.dto;

public record LineaPedido(Long productoId, String nombre, Long precioUnitarioClp, Integer cantidad, Long subtotalClp) {}
