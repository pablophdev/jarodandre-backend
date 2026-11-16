package cl.jajoyeria.pedidos.dto;

import java.math.BigDecimal;

public record ProductoVendido(Long productoId, String nombre, Long unidades, BigDecimal ingresosClp) {}
