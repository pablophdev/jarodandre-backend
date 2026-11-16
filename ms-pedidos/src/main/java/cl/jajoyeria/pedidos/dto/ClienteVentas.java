package cl.jajoyeria.pedidos.dto;

import java.math.BigDecimal;

public record ClienteVentas(String correo, Long pedidos, BigDecimal totalClp) {}
