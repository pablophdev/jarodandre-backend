package cl.jajoyeria.pedidos.dto;

import java.util.*;

public record CarritoVista(UUID id, List<LineaCarrito> items, Long totalClp, Long pedidoPendienteId) {}
