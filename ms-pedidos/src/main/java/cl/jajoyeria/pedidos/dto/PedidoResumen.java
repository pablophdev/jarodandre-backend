package cl.jajoyeria.pedidos.dto;

import cl.jajoyeria.pedidos.entity.*;
import java.time.Instant;

public record PedidoResumen(Long id, String numero, Instant creadoEn, Long totalClp, EstadoPago estadoPago, EstadoEntrega estadoEntrega) {}
