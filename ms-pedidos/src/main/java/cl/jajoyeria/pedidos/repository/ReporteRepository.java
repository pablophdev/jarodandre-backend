package cl.jajoyeria.pedidos.repository;

import cl.jajoyeria.pedidos.dto.*;
import cl.jajoyeria.pedidos.entity.EstadoPago;
import java.math.*;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ReporteRepository {
    private final JdbcClient jdbc;

    public ReporteVentas ventas() {
        Map<String,Long> estados = new LinkedHashMap<>();
        for (var estado : EstadoPago.values()) estados.put(estado.name(), 0L);
        jdbc.sql("select estado_pago, count(*) from ventas.pedidos group by estado_pago")
                .query((rs,i) -> Map.entry(rs.getString(1), rs.getLong(2))).list()
                .forEach(entry -> estados.put(entry.getKey(), entry.getValue()));
        BigDecimal total = jdbc.sql("select coalesce(sum(total_clp),0) from ventas.pedidos where estado_pago='PAGADO'")
                .query(BigDecimal.class).single();
        long cantidad = estados.get("PAGADO");
        var productos = jdbc.sql("""
                select i.producto_id, max(i.nombre_producto), sum(i.cantidad), sum(i.subtotal_clp)
                from ventas.pedido_items i join ventas.pedidos p on p.id=i.pedido_id
                where p.estado_pago='PAGADO' group by i.producto_id
                order by sum(i.cantidad) desc, i.producto_id limit 10
                """).query((rs,i) -> new ProductoVendido(rs.getLong(1), rs.getString(2), rs.getLong(3), rs.getBigDecimal(4))).list();
        var clientes = jdbc.sql("""
                select correo_entrega, count(*), sum(total_clp) from ventas.pedidos
                where estado_pago='PAGADO' group by correo_entrega order by sum(total_clp) desc, correo_entrega limit 10
                """).query((rs,i) -> new ClienteVentas(rs.getString(1), rs.getLong(2), rs.getBigDecimal(3))).list();
        BigDecimal promedio = cantidad == 0 ? BigDecimal.ZERO : total.divide(BigDecimal.valueOf(cantidad), 0, RoundingMode.HALF_UP);
        return new ReporteVentas(total, promedio, estados, productos, clientes);
    }
}
