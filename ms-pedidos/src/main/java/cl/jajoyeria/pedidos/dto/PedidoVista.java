package cl.jajoyeria.pedidos.dto;

import cl.jajoyeria.pedidos.entity.*;
import java.time.Instant;
import java.util.*;

public record PedidoVista(Long id, String numero, UUID usuarioId, Instant creadoEn, Entrega entrega, List<LineaPedido> items,
        Long totalClp, EstadoPago estadoPago, EstadoEntrega estadoEntrega, Instant pagadoEn) {}
