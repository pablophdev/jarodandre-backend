package cl.jajoyeria.catalogo.dto;

import cl.jajoyeria.catalogo.entity.Disponibilidad;

public record PrecioResponse(
        Long productoId, String nombre, Long precioClp, Disponibilidad disponibilidad) {}
