package cl.jajoyeria.pedidos.dto;

import java.math.BigDecimal;
import java.util.*;

public record ReporteVentas(BigDecimal ingresosClp, BigDecimal ticketPromedioClp, Map<String, Long> pedidosPorEstado,
        List<ProductoVendido> productosMasVendidos, List<ClienteVentas> mejoresClientes) {}
