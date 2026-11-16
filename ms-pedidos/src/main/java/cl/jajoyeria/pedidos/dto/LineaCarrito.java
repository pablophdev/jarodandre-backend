package cl.jajoyeria.pedidos.dto;

public record LineaCarrito(Long productoId, Integer cantidad, String nombre, Long precioClp, String disponibilidad, Long subtotalClp) {}
